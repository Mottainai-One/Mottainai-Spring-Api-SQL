package com.institutojf.mottainai.dto.response;

import java.math.BigDecimal;

public record CustomerCatalogStoreResponse(
    Integer id,
    String name,
    Address address,
    BigDecimal latitude,
    BigDecimal longitude
) {
    public record Address(
        String zipCode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state
    ) {}
}
