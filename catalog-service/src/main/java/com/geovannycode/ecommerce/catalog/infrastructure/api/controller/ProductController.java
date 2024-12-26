package com.geovannycode.ecommerce.catalog.infrastructure.api.controller;

import com.geovannycode.ecommerce.catalog.application.ports.input.GetProductByCodeUseCase;
import com.geovannycode.ecommerce.catalog.application.ports.input.GetProductsUseCase;
import com.geovannycode.ecommerce.catalog.domain.model.PagedResult;
import com.geovannycode.ecommerce.catalog.infrastructure.api.dto.ProductDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final GetProductsUseCase getProductsUseCase;
    private final GetProductByCodeUseCase getProductByCodeUseCase;

    public ProductController(GetProductsUseCase getProductsUseCase, GetProductByCodeUseCase getProductByCodeUseCase) {
        this.getProductsUseCase = getProductsUseCase;
        this.getProductByCodeUseCase = getProductByCodeUseCase;
    }

    @GetMapping
    PagedResult<ProductDto> getProducts(@RequestParam(name = "page", defaultValue = "1") int pageNo) {
        return getProductsUseCase.getProducts(pageNo);
    }

    @GetMapping("/{code}")
    ResponseEntity<ProductDto> getProductByCode(@PathVariable String code) {
        return getProductByCodeUseCase
                .getProductByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
