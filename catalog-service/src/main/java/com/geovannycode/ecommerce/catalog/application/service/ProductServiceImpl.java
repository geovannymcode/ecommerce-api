package com.geovannycode.ecommerce.catalog.application.service;

import com.geovannycode.ecommerce.catalog.application.ports.input.GetProductByCodeUseCase;
import com.geovannycode.ecommerce.catalog.application.ports.input.GetProductsUseCase;
import com.geovannycode.ecommerce.catalog.application.ports.output.ProductRepository;
import com.geovannycode.ecommerce.catalog.domain.exception.ProductNotFoundException;
import com.geovannycode.ecommerce.catalog.domain.model.PagedResult;
import com.geovannycode.ecommerce.catalog.infrastructure.api.dto.ProductDto;
import com.geovannycode.ecommerce.catalog.infrastructure.api.mapper.ProductMapper;
import com.geovannycode.ecommerce.catalog.infrastructure.config.ApplicationProperties;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements GetProductsUseCase, GetProductByCodeUseCase {

    private final ProductRepository productRepository;
    private final ApplicationProperties properties;

    public ProductServiceImpl(ProductRepository productRepository, ApplicationProperties properties) {
        this.productRepository = productRepository;
        this.properties = properties;
    }

    @Override
    public PagedResult<ProductDto> getProducts(int pageNo) {
        Sort sort = Sort.by("name").ascending();
        pageNo = pageNo <= 1 ? 0 : pageNo - 1;
        Pageable pageable = PageRequest.of(pageNo, properties.pageSize(), sort);
        Page<ProductDto> productsPage = productRepository.findAll(pageable).map(ProductMapper::toDto);

        return new PagedResult<>(
                productsPage.getContent(),
                productsPage.getTotalElements(),
                productsPage.getNumber() + 1,
                productsPage.getTotalPages(),
                productsPage.isFirst(),
                productsPage.isLast(),
                productsPage.hasNext(),
                productsPage.hasPrevious());
    }

    @Override
    public Optional<ProductDto> getProductByCode(String code) {
        return Optional.ofNullable(productRepository
                .findByCode(code)
                .map(ProductMapper::toDto)
                .orElseThrow(() -> ProductNotFoundException.forCode(code)));
    }
}
