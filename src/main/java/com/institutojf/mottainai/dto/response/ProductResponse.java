package com.institutojf.mottainai.dto.response;

import java.math.BigDecimal;

public record ProductResponse(
        Integer id,
        Integer categoryId,
        String categoryName,
        String barcode,
        String name,
        String description,
        String brand,
        String unitMeasure,
        BigDecimal weight,
        Boolean active,
        Integer version
) {
}
