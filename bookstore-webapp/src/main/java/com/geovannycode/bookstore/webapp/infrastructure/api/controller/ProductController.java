package com.geovannycode.bookstore.webapp.infrastructure.api.controller;

import com.geovannycode.bookstore.webapp.domain.model.PagedResult;
import com.geovannycode.bookstore.webapp.domain.model.Product;
import com.geovannycode.bookstore.webapp.infrastructure.clients.catalog.CatalogServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final CatalogServiceClient catalogService;

    ProductController(CatalogServiceClient catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/api/products")
    @ResponseBody
    public PagedResult<Product> products(@RequestParam(name = "page", defaultValue = "0") int page) {
        log.info("API - Solicitando productos para la página: {}", page);
        PagedResult<Product> result = catalogService.getProducts(page);
        log.info(
                "API - Recibidos productos: {} items, página {} de {}",
                result.data().size(),
                result.pageNumber() + 1,
                result.totalPages());
        return result;
    }

    /**
     * Obtiene un producto por su código
     * @param code Código del producto
     * @return Producto encontrado
     * @throws ResponseStatusException si el producto no se encuentra
     */
    @GetMapping("/api/products/{code}")
    @ResponseBody
    public Product getProductByCode(@PathVariable("code") String code) {
        log.info("API - Buscando producto con código: {}", code);
        var response = catalogService.getProductByCode(code);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            log.info("API - Producto encontrado: {}", response.getBody().name());
            return response.getBody();
        }
        log.warn("API - Producto no encontrado con código: {}", code);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
    }
}
