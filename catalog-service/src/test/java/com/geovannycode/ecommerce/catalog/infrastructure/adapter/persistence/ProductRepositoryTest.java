package com.geovannycode.ecommerce.catalog.infrastructure.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.geovannycode.ecommerce.catalog.domain.model.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest(
        properties = {
            "spring.test.database.replace=none",
            "spring.datasource.url=jdbc:tc:postgresql:16-alpine:///db",
        })
@Sql("/test-data.sql")
public class ProductRepositoryTest {

    @Autowired
    SpringDataProductRepository springDataProductRepository;

    @Test
    void shouldGetAllProducts() {
        // Configuramos la paginación para obtener todos los productos
        Pageable pageable = PageRequest.of(0, 20, Sort.by("name").ascending());
        Page<Product> productPage = springDataProductRepository.findAll(pageable);

        // Verificamos que se obtengan 15 productos
        assertThat(productPage.getContent()).hasSize(15);

        // Validamos algunos productos (opcional)
        Product firstProduct = productPage.getContent().get(0);
        assertThat(firstProduct.getStock()).isGreaterThan(0); // Validamos que el stock sea mayor a 0
    }

    @Test
    void shouldGetProductByCode() {
        Product product = springDataProductRepository.findByCode("P100").orElseThrow();
        assertThat(product.getCode()).isEqualTo("P100");
        assertThat(product.getName()).isEqualTo("The Hunger Games");
        assertThat(product.getDescription()).isEqualTo("Winning will make you famous. Losing means certain death...");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("34.0"));
        assertThat(product.getStock()).isEqualTo(50);
    }

    @Test
    void shouldReturnEmptyWhenProductCodeNotExists() {
        assertThat(springDataProductRepository.findByCode("invalid_product_code"))
                .isEmpty();
    }
}
