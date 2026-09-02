package com.institutojf.mottainai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateLoyaltyRewardRequest(
    @NotBlank String name,
    String description,
    @NotNull Integer pointsCost,
    Boolean active,
    LocalDateTime validFrom,
    LocalDateTime validUntil
) {
}
