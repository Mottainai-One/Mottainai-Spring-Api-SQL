package com.institutojf.mottainai.dto.response;

import java.math.BigDecimal;

public record SupplierProductResponse(
        Integer id,
        Integer supplierId,
        String supplierTradeName,
        Integer productId,
        String productName,
        String supplierCode,
        BigDecimal purchasePrice,
        Integer leadTime,
        Boolean active
) {
}
