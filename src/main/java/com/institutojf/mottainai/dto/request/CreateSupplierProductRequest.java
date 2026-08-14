package com.institutojf.mottainai.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateSupplierProductRequest(
        @NotNull Integer supplierId,
        @NotNull Integer productId,
        @Size(max = 50) String supplierCode,
        @NotNull @DecimalMin(value = "0.00") @Digits(integer = 8, fraction = 2) BigDecimal purchasePrice,
        @NotNull @PositiveOrZero Integer leadTime
) {
}
