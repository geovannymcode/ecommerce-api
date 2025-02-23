package com.geovannycode.ecommerce.catalog.infrastructure.api.controller;

import com.geovannycode.ecommerce.catalog.application.ports.input.CreateProductUseCase;
import com.geovannycode.ecommerce.catalog.application.ports.input.DeleteProductUseCase;
import com.geovannycode.ecommerce.catalog.application.ports.input.GetProductByCodeUseCase;
import com.geovannycode.ecommerce.catalog.application.ports.input.GetProductsUseCase;
import com.geovannycode.ecommerce.catalog.application.ports.input.SearchProductsUseCase;
import com.geovannycode.ecommerce.catalog.domain.model.PagedResult;
import com.geovannycode.ecommerce.catalog.infrastructure.api.dto.ProductDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final GetProductsUseCase getProductsUseCase;
    private final GetProductByCodeUseCase getProductByCodeUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final SearchProductsUseCase searchProductsUseCase;

    public ProductController(
            GetProductsUseCase getProductsUseCase,
            GetProductByCodeUseCase getProductByCodeUseCase,
            CreateProductUseCase createProductUseCase,
            DeleteProductUseCase deleteProductUseCase,
            SearchProductsUseCase searchProductsUseCase) {
        this.getProductsUseCase = getProductsUseCase;
        this.getProductByCodeUseCase = getProductByCodeUseCase;
        this.createProductUseCase = createProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.searchProductsUseCase = searchProductsUseCase;
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

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody @Valid ProductDto productDto) {

        ProductDto createdProduct = createProductUseCase.createProduct(productDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @DeleteMapping("/{code}")
    public void deleteProduct(@PathVariable String code) {
        deleteProductUseCase.deleteProduct(code);
    }

    @GetMapping("/search")
    public PagedResult<ProductDto> searchProducts(
            @RequestParam(name = "query") String query,
            @RequestParam(required = false, defaultValue = "1", name = "page") int page) {
        return searchProductsUseCase.searchProductsByCriteria(query, page);
    }
}
