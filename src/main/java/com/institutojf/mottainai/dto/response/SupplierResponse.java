package com.institutojf.mottainai.dto.response;

public record SupplierResponse(
        Integer id,
        AddressResponse address,
        String tradeName,
        String cnpj,
        String email,
        String phone,
        Boolean active
) {
}
