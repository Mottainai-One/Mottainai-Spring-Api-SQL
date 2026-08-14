package com.institutojf.mottainai.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotNull Integer categoryId,
        @NotBlank @Size(max = 30) String barcode,
        @NotBlank @Size(max = 150) String name,
        String description,
        @Size(max = 100) String brand,
        @NotBlank @Size(max = 20) String unitMeasure,
        @DecimalMin(value = "0.000") @Digits(integer = 7, fraction = 3) BigDecimal weight
) {
}
