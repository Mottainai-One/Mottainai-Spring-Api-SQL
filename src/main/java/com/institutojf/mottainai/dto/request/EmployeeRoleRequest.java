package com.institutojf.mottainai.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeeRoleRequest(
        @NotBlank @Size(max = 80) String name,
        String description,
        @NotNull @Min(0) Integer permissionLevel
) {
}
