package com.geovannycode.ecommerce.catalog.infrastructure.adapter.persistence;

import com.geovannycode.ecommerce.catalog.domain.model.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.code = :code AND p.deleted = false")
    Optional<Product> findByCode(String code);

    Page<Product> findAll(Pageable pageable);

    boolean existsProductByCode(String code);

    void deleteByCode(String code);

    @Query("""
    SELECT p FROM Product p
    WHERE p.name LIKE %:query%
    ORDER BY p.name ASC
""")
    Page<Product> searchProductsByCriteria(@Param("query") String query, Pageable pageable);
}
