package com.institutojf.mottainai.dto.response;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
