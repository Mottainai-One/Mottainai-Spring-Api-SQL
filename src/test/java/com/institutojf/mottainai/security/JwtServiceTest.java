package com.institutojf.mottainai.security;

import com.institutojf.mottainai.repository.AppUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
        .encodeToString("01234567890123456789012345678901".getBytes());

    @Test
    @DisplayName("Should issue a signed token with subject, roles and session")
    void shouldIssueASignedTokenWithSubjectRolesAndSession() {
        JwtProperties properties = new JwtProperties(SECRET, "https://mottainai.local", 60, 7);
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        SecurityConfig securityConfig = new SecurityConfig(properties, appUserRepository);
        JwtEncoder encoder = securityConfig.jwtEncoder();
        JwtDecoder decoder = securityConfig.jwtDecoder();
        JwtService jwtService = new JwtService(encoder, properties);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("manager@mottainai.com", null, "ROLE_MANAGER");
        UUID sessionId = UUID.randomUUID();
        String tokenValue = jwtService.generateAccessToken(authentication, sessionId);
        Jwt token = decoder.decode(tokenValue);
        assertEquals("manager@mottainai.com", token.getSubject());
        assertEquals("https://mottainai.local", token.getClaimAsString("iss"));
        assertEquals(List.of("MANAGER"), token.getClaimAsStringList("roles"));
        assertNull(token.getClaim("tokenVersion"));
        assertEquals("access", token.getClaimAsString("use"));
        assertEquals(sessionId.toString(), token.getClaimAsString("sid"));
        assertTrue(token.getExpiresAt().isAfter(token.getIssuedAt()));
    }

    @Test
    @DisplayName("Should issue a refresh token with its own purpose")
    void shouldIssueARefreshTokenWithItsOwnPurpose() {
        JwtProperties properties = new JwtProperties(SECRET, "https://mottainai.local", 60, 7);
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        SecurityConfig securityConfig = new SecurityConfig(properties, appUserRepository);
        JwtService jwtService = new JwtService(securityConfig.jwtEncoder(), properties);
        String tokenValue = jwtService.generateRefreshToken(new TestingAuthenticationToken("manager@mottainai.com", null, "ROLE_MANAGER"), UUID.randomUUID(), 7);
        Jwt token = securityConfig.jwtDecoder().decode(tokenValue);
        assertEquals("refresh", token.getClaimAsString("use"));
        assertTrue(token.getExpiresAt().isAfter(token.getIssuedAt()));
    }

}
