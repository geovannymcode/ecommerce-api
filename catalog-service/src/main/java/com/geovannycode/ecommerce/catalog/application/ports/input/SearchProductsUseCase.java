package com.geovannycode.ecommerce.catalog.application.ports.input;

import com.geovannycode.ecommerce.catalog.domain.model.PagedResult;
import com.geovannycode.ecommerce.catalog.infrastructure.api.dto.ProductDto;

public interface SearchProductsUseCase {
    PagedResult<ProductDto> searchProductsByCriteria(String query, int page);
}
