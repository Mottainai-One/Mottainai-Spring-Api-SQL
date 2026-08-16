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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordResetEmailService passwordResetEmailService;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Should return bearer token and update last login after authentication")
    void shouldReturnBearerTokenAndUpdateLastLoginAfterAuthentication() {
        LoginRequest request = new LoginRequest("manager@mottainai.com", "password");
        AppUser user = user(request.email());
        user.setTokenVersion(2);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(request.email(), null, "ROLE_MANAGER");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(authentication, 2)).thenReturn("signed-token");
        when(jwtProperties.expirationMinutes()).thenReturn(60L);

        TokenResponse response = authenticationService.login(request);

        assertEquals("signed-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600, response.expiresIn());
        verify(jwtService).generateToken(authentication, 2);
        verify(appUserRepository).save(user);
    }

    @Test
    @DisplayName("Should create a hashed recovery code for an existing user")
    void shouldCreateHashedRecoveryCodeForAnExistingUser() {
        AppUser user = user("user@mottainai.com");
        when(appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenAnswer(invocation -> "hash-" + invocation.getArgument(0));
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        authenticationService.requestPasswordReset(new ForgotPasswordRequest(user.getEmail()));

        verify(passwordResetTokenRepository).deleteByUserId(user.getId());
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        verify(passwordResetEmailService).sendRecoveryCode(eq(user.getEmail()), codeCaptor.capture());
        assertTrue(codeCaptor.getValue().matches("\\d{6}"));
        assertEquals("hash-" + codeCaptor.getValue(), tokenCaptor.getValue().getCodeHash());
        assertEquals(0, tokenCaptor.getValue().getAttempts());
        assertTrue(tokenCaptor.getValue().getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(9)));
    }

    @Test
    @DisplayName("Should not reveal when the recovery email does not exist")
    void shouldNotRevealWhenTheRecoveryEmailDoesNotExist() {
        String email = "unknown@mottainai.com";
        when(appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(email)).thenReturn(Optional.empty());

        authenticationService.requestPasswordReset(new ForgotPasswordRequest(email));

        verify(passwordResetTokenRepository, never()).save(any());
        verify(passwordResetEmailService, never()).sendRecoveryCode(any(), any());
    }

    @Test
    @DisplayName("Should reset password, use code and invalidate previous tokens")
    void shouldResetPasswordUseCodeAndInvalidatePreviousTokens() {
        AppUser user = user("user@mottainai.com");
        user.setTokenVersion(0);
        PasswordResetToken token = activeToken(user);
        ResetPasswordRequest request = new ResetPasswordRequest(user.getEmail(), "123456", "new-password");
        when(appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId())).thenReturn(Optional.of(token));
        when(passwordEncoder.matches(request.code(), token.getCodeHash())).thenReturn(true);
        when(passwordEncoder.encode(request.newPassword())).thenReturn("new-password-hash");

        authenticationService.resetPassword(request);

        assertEquals("new-password-hash", user.getPasswordHash());
        assertEquals(1, user.getTokenVersion());
        assertTrue(token.getUsedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        verify(appUserRepository).save(user);
        verify(passwordResetTokenRepository).save(token);
    }

    @Test
    @DisplayName("Should count invalid recovery code attempts")
    void shouldCountInvalidRecoveryCodeAttempts() {
        AppUser user = user("user@mottainai.com");
        PasswordResetToken token = activeToken(user);
        ResetPasswordRequest request = new ResetPasswordRequest(user.getEmail(), "000000", "new-password");
        when(appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId())).thenReturn(Optional.of(token));
        when(passwordEncoder.matches(request.code(), token.getCodeHash())).thenReturn(false);

        assertThrows(BusinessException.class, () -> authenticationService.resetPassword(request));

        assertEquals(1, token.getAttempts());
        verify(passwordResetTokenRepository).save(token);
        verify(appUserRepository, never()).save(user);
    }

    @Test
    @DisplayName("Should reject an expired recovery code")
    void shouldRejectAnExpiredRecoveryCode() {
        AppUser user = user("user@mottainai.com");
        PasswordResetToken token = activeToken(user);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        ResetPasswordRequest request = new ResetPasswordRequest(user.getEmail(), "123456", "new-password");
        when(appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId())).thenReturn(Optional.of(token));

        assertThrows(BusinessException.class, () -> authenticationService.resetPassword(request));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(appUserRepository, never()).save(user);
    }

    private AppUser user(String email) {
        AppUser user = new AppUser();
        user.setId(1);
        user.setEmail(email);
        user.setTokenVersion(0);
        return user;
    }

    private PasswordResetToken activeToken(AppUser user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCodeHash("code-hash");
        token.setAttempts(0);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        return token;
    }
}
