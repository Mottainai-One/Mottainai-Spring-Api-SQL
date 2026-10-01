package com.institutojf.mottainai.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateLoyaltyStatusRequest(
        @NotNull Boolean active
) {
}
