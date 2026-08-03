package com.institutojf.mottainai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSupplierRequest(
        @NotNull Integer addressId,
        @NotBlank @Size(max = 150) String tradeName,
        @Email @Size(max = 150) String email,
        @Size(max = 20) String phone,
        @NotNull Boolean active
) {
}
