package com.institutojf.mottainai.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes());

    @Test
    @DisplayName("Should issue a signed token with subject and roles")
    void shouldIssueASignedTokenWithSubjectAndRoles() {
        JwtProperties properties = new JwtProperties(SECRET, "https://mottainai.local", 60);
        SecurityConfig securityConfig = new SecurityConfig(properties);
        JwtEncoder encoder = securityConfig.jwtEncoder();
        JwtDecoder decoder = securityConfig.jwtDecoder();
        JwtService jwtService = new JwtService(encoder, properties);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                "manager@mottainai.com", null, "ROLE_MANAGER"
        );

        String tokenValue = jwtService.generateToken(authentication);
        Jwt token = decoder.decode(tokenValue);

        assertEquals("manager@mottainai.com", token.getSubject());
        assertEquals("https://mottainai.local", token.getClaimAsString("iss"));
        assertEquals(List.of("MANAGER"), token.getClaimAsStringList("roles"));
        assertTrue(token.getExpiresAt().isAfter(token.getIssuedAt()));
    }
}
