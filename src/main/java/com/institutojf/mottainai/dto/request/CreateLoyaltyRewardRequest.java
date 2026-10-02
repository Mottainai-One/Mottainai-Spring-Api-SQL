package com.institutojf.mottainai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

public record CreateLoyaltyRewardRequest(
        @NotBlank String name,
        String description,
        @NotNull @Min(1) Integer pointsCost,
        Boolean active,
        LocalDateTime validFrom,
        LocalDateTime validUntil
) {
}
