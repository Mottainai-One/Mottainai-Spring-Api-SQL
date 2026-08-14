package com.institutojf.mottainai.dto.response;

public record ProductCategoryResponse(
        Integer id,
        String name,
        String description,
        Boolean active
) {
}
