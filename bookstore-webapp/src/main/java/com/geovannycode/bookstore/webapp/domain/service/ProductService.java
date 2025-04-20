package com.geovannycode.bookstore.webapp.domain.service;

import com.geovannycode.bookstore.webapp.domain.model.PagedResult;
import com.geovannycode.bookstore.webapp.domain.model.Product;
import com.geovannycode.bookstore.webapp.infrastructure.api.controller.ProductController;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Servicio que actúa como fachada entre la capa de presentación y la capa de API
 * facilitando el acceso a los productos desde cualquier interfaz de usuario.
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductController productController;

    public ProductService(ProductController productController) {
        this.productController = productController;
    }

    public PagedResult<Product> getProducts(int page) {
        try {
            log.debug("Solicitando productos para la página {}", page);
            PagedResult<Product> result = productController.products(page);
            log.debug(
                    "Recibido: {} productos. Página: {}, Total páginas: {}, Total elementos: {}",
                    result.data().size(),
                    result.pageNumber(),
                    result.totalPages(),
                    result.totalElements());


            if (result.pageNumber() != page) {
                log.warn("La página solicitada ({}) no coincide con la recibida ({})", page, result.pageNumber());
            }

            return result;
        } catch (ResourceAccessException e) {
            log.error("Error de conexión al obtener productos para la página {}: {}", page, e.getMessage());
            return new PagedResult<>(Collections.emptyList(), 0, 0, 0, true, true, false, false);
        } catch (Exception e) {
            log.error("Error al obtener productos para la página {}: {}", page, e.getMessage(), e);
            throw e;
        }
    }

    public Product getProductByCode(String code) {
        try {
            log.debug("Buscando producto con código: {}", code);
            Product product = productController.getProductByCode(code);
            log.debug("Producto encontrado: {}", product.name());
            return product;
        } catch (ResourceAccessException e) {
            log.error("Error de conexión al buscar producto con código {}: {}", code, e.getMessage());
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo conectar al servicio de catálogo. Inténtelo de nuevo más tarde.");
        } catch (ResponseStatusException e) {
            log.warn("Producto no encontrado con código: {}", code);
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener producto con código {}: {}", code, e.getMessage(), e);
            throw e;
        }
    }
}
