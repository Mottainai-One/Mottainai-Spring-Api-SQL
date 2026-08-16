package com.institutojf.mottainai.security;

import com.institutojf.mottainai.model.AppUser;
import com.institutojf.mottainai.repository.AppUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes());

    @Test
    @DisplayName("Should issue a signed token with subject, roles and token version")
    void shouldIssueASignedTokenWithSubjectRolesAndTokenVersion() {
        JwtProperties properties = new JwtProperties(SECRET, "https://mottainai.local", 60);
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        AppUser user = new AppUser();
        user.setTokenVersion(2);
        when(appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull("manager@mottainai.com"))
                .thenReturn(Optional.of(user));
        SecurityConfig securityConfig = new SecurityConfig(properties, appUserRepository);
        JwtEncoder encoder = securityConfig.jwtEncoder();
        JwtDecoder decoder = securityConfig.jwtDecoder();
        JwtService jwtService = new JwtService(encoder, properties);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                "manager@mottainai.com", null, "ROLE_MANAGER"
        );

        String tokenValue = jwtService.generateToken(authentication, 2);
        Jwt token = decoder.decode(tokenValue);

        assertEquals("manager@mottainai.com", token.getSubject());
        assertEquals("https://mottainai.local", token.getClaimAsString("iss"));
        assertEquals(List.of("MANAGER"), token.getClaimAsStringList("roles"));
        assertEquals(2, ((Number) token.getClaim("tokenVersion")).intValue());
        assertTrue(token.getExpiresAt().isAfter(token.getIssuedAt()));
    }

    @Test
    @DisplayName("Should reject a token after its user token version changes")
    void shouldRejectATokenAfterItsUserTokenVersionChanges() {
        JwtProperties properties = new JwtProperties(SECRET, "https://mottainai.local", 60);
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        AppUser user = new AppUser();
        user.setTokenVersion(0);
        when(appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull("manager@mottainai.com"))
                .thenReturn(Optional.of(user));
        SecurityConfig securityConfig = new SecurityConfig(properties, appUserRepository);
        JwtService jwtService = new JwtService(securityConfig.jwtEncoder(), properties);
        String tokenValue = jwtService.generateToken(
                new TestingAuthenticationToken("manager@mottainai.com", null, "ROLE_MANAGER"), 0
        );
        user.setTokenVersion(1);

        JwtDecoder decoder = securityConfig.jwtDecoder();

        assertThrows(JwtValidationException.class, () -> decoder.decode(tokenValue));
    }
}
