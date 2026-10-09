package com.institutojf.mottainai.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateLoyaltyRewardRequest(
        @NotNull @Min(1) Integer pointsCost
) {
}
