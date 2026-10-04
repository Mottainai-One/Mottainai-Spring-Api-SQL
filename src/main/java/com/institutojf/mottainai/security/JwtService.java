package com.institutojf.mottainai.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    private final JwtProperties jwtProperties;

    public String generateAccessToken(Authentication authentication, UUID sessionId) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.expirationMinutes(), ChronoUnit.MINUTES);
        return generateToken(authentication, sessionId, issuedAt, expiresAt, "access");
    }

    public String generateRefreshToken(Authentication authentication, UUID sessionId, long expirationDays) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expirationDays, ChronoUnit.DAYS);
        return generateToken(authentication, sessionId, issuedAt, expiresAt, "refresh");
    }

    private String generateToken(Authentication authentication, UUID sessionId, Instant issuedAt, Instant expiresAt,
            String use) {
        List<String> roles = authentication.getAuthorities()
            .stream()
            .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
            .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer())
            .subject(authentication.getName())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .claim("roles", roles)
            // Vincula o token à sessão rotacionável e revogável persistida no banco
            .claim("sid", sessionId.toString())
            .claim("use", use)
            .id(UUID.randomUUID().toString())
            .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

}
