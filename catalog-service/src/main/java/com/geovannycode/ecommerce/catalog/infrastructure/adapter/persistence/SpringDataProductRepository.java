package com.geovannycode.ecommerce.catalog.infrastructure.adapter.persistence;

import com.geovannycode.ecommerce.catalog.domain.model.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByCode(String code);
}
