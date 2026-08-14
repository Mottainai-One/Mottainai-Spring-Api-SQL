package com.institutojf.mottainai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProductCategoryRequest(
        @NotBlank @Size(max = 100) String name,
        String description
) {
}
