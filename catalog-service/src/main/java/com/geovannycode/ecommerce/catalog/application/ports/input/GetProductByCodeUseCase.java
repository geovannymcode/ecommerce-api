package com.geovannycode.ecommerce.catalog.application.ports.input;

import com.geovannycode.ecommerce.catalog.infrastructure.api.dto.ProductDto;
import java.util.Optional;

public interface GetProductByCodeUseCase {
    Optional<ProductDto> getProductByCode(String code);
}
