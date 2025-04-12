package com.geovannycode.ecommerce.catalog.application.service;

import com.geovannycode.ecommerce.catalog.application.ports.input.CreateProductUseCase;
import com.geovannycode.ecommerce.catalog.application.ports.input.DeleteProductUseCase;
import com.geovannycode.ecommerce.catalog.application.ports.input.GetProductByCodeUseCase;
import com.geovannycode.ecommerce.catalog.application.ports.input.GetProductsUseCase;
import com.geovannycode.ecommerce.catalog.application.ports.input.SearchProductsUseCase;
import com.geovannycode.ecommerce.catalog.domain.exception.ProductAlreadyExistsException;
import com.geovannycode.ecommerce.catalog.domain.exception.ProductNotFoundException;
import com.geovannycode.ecommerce.catalog.domain.model.PagedResult;
import com.geovannycode.ecommerce.catalog.domain.model.Product;
import com.geovannycode.ecommerce.catalog.domain.port.output.ProductRepository;
import com.geovannycode.ecommerce.catalog.infrastructure.api.dto.ProductDto;
import com.geovannycode.ecommerce.catalog.infrastructure.api.mapper.ProductMapper;
import com.geovannycode.ecommerce.catalog.infrastructure.config.ApplicationProperties;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl
        implements GetProductsUseCase,
                GetProductByCodeUseCase,
                CreateProductUseCase,
                DeleteProductUseCase,
                SearchProductsUseCase {

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

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        boolean existsProductByCode = productRepository.existsProductByCode(productDto.code());
        if (existsProductByCode) {
            throw new ProductAlreadyExistsException(productDto.code());
        }
        Product product = ProductMapper.toEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    @Override
    public void deleteProduct(String code) {
        Product product = productRepository.findByCode(code).orElseThrow(() -> new ProductNotFoundException(code));
        product.setDeleted(true);
        productRepository.save(product);
    }

    @Override
    public PagedResult<ProductDto> searchProductsByCriteria(String query, int page) {
        Sort sort = Sort.by("name").ascending();
        Pageable pageable = PageRequest.of(page - 1, properties.pageSize(), sort);
        Page<Product> productPage = productRepository.searchProductsByCriteria(query, pageable);
        var productDtos =
                productPage.getContent().stream().map(ProductMapper::toDto).collect(Collectors.toList());
        return new PagedResult<>(
                productDtos,
                productPage.getTotalElements(),
                productPage.getNumber() + 1,
                productPage.getTotalPages(),
                productPage.isFirst(),
                productPage.isLast(),
                productPage.hasNext(),
                productPage.hasPrevious());
    }
}
