package com.institutojf.mottainai.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRetailStoreRequest(
        @NotNull Integer companyId,
        @NotNull Integer addressId,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = "\\d{14}") String cnpj,
        @Email @Size(max = 150) String email,
        @Size(max = 20) String phone,
        @DecimalMin("-90.0") @DecimalMax("90.0") java.math.BigDecimal latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") java.math.BigDecimal longitude
) {
}
