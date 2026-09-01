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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Locale;

@Service
public class AuthenticationService {

    private static final int RATE_LIMIT_MINUTES = 5;
    private static final int RESET_CODE_EXPIRATION_MINUTES = 10;
    private static final int MAX_RESET_ATTEMPTS = 5;

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetEmailService passwordResetEmailService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${security.password.predictable-terms:mottainai,2026}")
    private String predictableTermsConfig = "mottainai,2026";

    public AuthenticationService(AuthenticationManager authenticationManager, AppUserRepository appUserRepository,
                                 PasswordResetTokenRepository passwordResetTokenRepository, PasswordResetEmailService passwordResetEmailService,
                                 JwtService jwtService, JwtProperties jwtProperties, PasswordEncoder passwordEncoder) {
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
        user.setLastLogin(LocalDateTime.now(ZoneOffset.UTC));
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
        validateNewPassword(request.newPassword());

        AppUser user = appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(request.email())
                .orElse(null);
        PasswordResetToken token = user != null
                ? passwordResetTokenRepository.findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId()).orElse(null)
                : null;

        // Sempre executa BCrypt para ter tempo constante
        String dummyHash = passwordEncoder.encode("dummy");
        if (token == null || token.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC)) || token.getAttempts() >= MAX_RESET_ATTEMPTS) {
            passwordEncoder.matches(request.code(), dummyHash);
            throw invalidRecoveryCode();
        }

        if (!passwordEncoder.matches(request.code(), token.getCodeHash())) {
            token.setAttempts(token.getAttempts() + 1);
            passwordResetTokenRepository.save(token);
            throw invalidRecoveryCode();
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        token.setUsedAt(LocalDateTime.now(ZoneOffset.UTC));
        appUserRepository.save(user);
        passwordResetTokenRepository.save(token);
    }

    private void createAndSendResetCode(AppUser user) {
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String code = generateResetCode();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCodeHash(passwordEncoder.encode(code));
        token.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(RESET_CODE_EXPIRATION_MINUTES));
        token.setAttempts(0);
        token.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        passwordResetTokenRepository.save(token);

        passwordResetEmailService.sendRecoveryCode(user.getEmail(), code);
    }

    private String generateResetCode() {
        return String.valueOf(secureRandom.nextInt(900_000) + 100_000);
    }

    private void validateNewPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessException("Password must be at least 8 characters long");
        }
        if (!hasRequiredCharacterTypes(password) || containsPredictableTerm(password)) {
            throw new BusinessException("Password must contain uppercase, lowercase, number and special character and must not contain predictable terms");
        }
    }

    private boolean hasRequiredCharacterTypes(String password) {
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasNumber = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialCharacter = password.chars()
                .anyMatch(character -> !Character.isLetterOrDigit(character) && !Character.isWhitespace(character));

        return hasUppercase && hasLowercase && hasNumber && hasSpecialCharacter;
    }

    private boolean containsPredictableTerm(String password) {
        String normalizedPassword = password.toLowerCase(Locale.ROOT);
        return Arrays.stream(predictableTermsConfig.split(","))
                .map(String::trim)
                .filter(term -> !term.isEmpty())
                .anyMatch(normalizedPassword::contains);
    }

    private BusinessException invalidRecoveryCode() {
        return new BusinessException("Invalid or expired recovery code");
    }

    private boolean hasRecentResetRequest(AppUser user) {
        return passwordResetTokenRepository
                .findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId())
                .map(token -> token.getCreatedAt().plusMinutes(RATE_LIMIT_MINUTES).isAfter(LocalDateTime.now(ZoneOffset.UTC)))
                .orElse(false);
    }

}
