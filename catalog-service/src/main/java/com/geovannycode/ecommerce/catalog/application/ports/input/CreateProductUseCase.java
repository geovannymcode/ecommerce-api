package com.geovannycode.ecommerce.catalog.application.ports.input;

import com.geovannycode.ecommerce.catalog.infrastructure.api.dto.ProductDto;

public interface CreateProductUseCase {
    ProductDto createProduct(ProductDto productDto);
}
