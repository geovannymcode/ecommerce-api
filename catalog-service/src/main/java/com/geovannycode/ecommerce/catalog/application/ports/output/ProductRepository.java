package com.geovannycode.ecommerce.catalog.application.ports.output;

import com.geovannycode.ecommerce.catalog.domain.model.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
    Optional<Product> findByCode(String code);

    Page<Product> findAll(Pageable pageable);
}
