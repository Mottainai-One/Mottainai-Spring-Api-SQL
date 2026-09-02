package com.institutojf.mottainai.dto.response;

public record CompanyResponse(
        Integer id,
        SubscriptionPlanResponse plan,
        String officialName,
        String tradeName,
        String cnpj,
        String email,
        String phone,
        java.math.BigDecimal latitude,
        java.math.BigDecimal longitude,
        Boolean active
) {
}
