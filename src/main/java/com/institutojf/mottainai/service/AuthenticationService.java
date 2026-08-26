package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.ForgotPasswordRequest;
import com.institutojf.mottainai.dto.request.LoginRequest;
import com.institutojf.mottainai.dto.request.ResetPasswordRequest;
import com.institutojf.mottainai.dto.response.TokenResponse;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.model.AppUser;
import com.institutojf.mottainai.model.PasswordResetToken;
import com.institutojf.mottainai.repository.AppUserRepository;
import com.institutojf.mottainai.repository.PasswordResetTokenRepository;
import com.institutojf.mottainai.security.JwtProperties;
import com.institutojf.mottainai.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Cuida do login e da recuperação de senha
 */
@Service
public class AuthenticationService {

    private static final int RATE_LIMIT_MINUTES = 5;
    private static final int RESET_CODE_EXPIRATION_MINUTES = 10;

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetEmailService passwordResetEmailService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            AppUserRepository appUserRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordResetEmailService passwordResetEmailService,
            JwtService jwtService,
            JwtProperties jwtProperties,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.appUserRepository = appUserRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordResetEmailService = passwordResetEmailService;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password())
        );
        AppUser user = appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(authentication.getName())
                .orElseThrow();
        user.setLastLogin(LocalDateTime.now());
        appUserRepository.save(user);

        return new TokenResponse(
                jwtService.generateToken(authentication, user.getTokenVersion()),
                "Bearer",
                jwtProperties.expirationMinutes() * 60
        );
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        // A resposta é a mesma para não revelar quais emails possuem conta
        appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(request.email())
                .ifPresent(user -> {
                    if (hasRecentResetRequest(user)) {
                        throw new BusinessException("Please wait before requesting another code");
                    }
                    createAndSendResetCode(user);
                });
    }

    /**
     * Atualiza a senha somente quando o código está válido
     * A versão do token muda para bloquear JWTs emitidos antes da troca
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public void resetPassword(ResetPasswordRequest request) {
        AppUser user = appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(request.email())
                .orElse(null);
        PasswordResetToken token = user != null
                ? passwordResetTokenRepository.findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId()).orElse(null)
                : null;

        // Sempre executa BCrypt para ter tempo constante
        String dummyHash = passwordEncoder.encode("dummy");
        if (token == null || token.getExpiresAt().isBefore(LocalDateTime.now()) || token.getAttempts() >= MAX_RESET_ATTEMPTS) {
            passwordEncoder.matches(request.code(), dummyHash);
            throw invalidRecoveryCode();
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now()) || token.getAttempts() >= MAX_RESET_ATTEMPTS) {
            throw invalidRecoveryCode();
        }

        if (!passwordEncoder.matches(request.code(), token.getCodeHash())) {
            token.setAttempts(token.getAttempts() + 1);
            passwordResetTokenRepository.save(token);
            throw invalidRecoveryCode();
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        token.setUsedAt(LocalDateTime.now());
        appUserRepository.save(user);
        passwordResetTokenRepository.save(token);
    }

    private void createAndSendResetCode(AppUser user) {
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String code = generateResetCode();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCodeHash(passwordEncoder.encode(code));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(RESET_CODE_EXPIRATION_MINUTES));
        token.setAttempts(0);
        token.setCreatedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);

        passwordResetEmailService.sendRecoveryCode(user.getEmail(), code);
    }

    private String generateResetCode() {
        return String.valueOf(secureRandom.nextInt(900_000) + 100_000);
    }

    private BusinessException invalidRecoveryCode() {
        return new BusinessException("Invalid or expired recovery code");
    }

    private boolean hasRecentResetRequest(AppUser user) {
        return passwordResetTokenRepository
                .findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId())
                .map(token -> token.getCreatedAt().plusMinutes(RATE_LIMIT_MINUTES).isAfter(LocalDateTime.now()))
                .orElse(false);
    }

}
