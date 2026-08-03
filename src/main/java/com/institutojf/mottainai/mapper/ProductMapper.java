package com.institutojf.mottainai.mapper;

import com.institutojf.mottainai.dto.response.ProductResponse;
import com.institutojf.mottainai.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getBarcode(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getUnitMeasure(),
                product.getWeight(),
                product.getActive(),
                product.getVersion()
        );
    }
}
