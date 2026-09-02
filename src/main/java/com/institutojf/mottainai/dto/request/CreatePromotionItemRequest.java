package com.institutojf.mottainai.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePromotionItemRequest(
    @NotNull Integer promotionId,
    @NotNull Integer productId,
    @NotNull BigDecimal originalPrice,
    @NotNull BigDecimal promotionalPrice,
    BigDecimal quantityAvailable
) {
}
