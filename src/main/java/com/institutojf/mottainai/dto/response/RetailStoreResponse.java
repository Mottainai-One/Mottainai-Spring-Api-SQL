package com.institutojf.mottainai.dto.response;

public record RetailStoreResponse(
        Integer id,
        CompanyResponse company,
        AddressResponse address,
        String name,
        String cnpj,
        String email,
        String phone,
        java.math.BigDecimal latitude,
        java.math.BigDecimal longitude,
        Boolean active
) {
}
