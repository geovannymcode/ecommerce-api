package com.geovannycode.ecommerce.catalog.infrastructure.api.mapper;

import com.geovannycode.ecommerce.catalog.domain.model.Product;
import com.geovannycode.ecommerce.catalog.infrastructure.api.dto.ProductDto;

public class ProductMapper {
    public static ProductDto toDto(Product product) {
        return new ProductDto(
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getPrice(),
                product.getStock());
    }
}
