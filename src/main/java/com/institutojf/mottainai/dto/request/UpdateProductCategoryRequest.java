package com.institutojf.mottainai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProductCategoryRequest(
        @NotBlank @Size(max = 100) String name,
        String description,
        @NotNull Boolean active
) {
}
