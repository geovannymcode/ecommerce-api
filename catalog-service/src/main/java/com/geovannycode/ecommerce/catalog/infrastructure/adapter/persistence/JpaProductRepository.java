package com.geovannycode.ecommerce.catalog.infrastructure.adapter.persistence;

import com.geovannycode.ecommerce.catalog.application.ports.output.ProductRepository;
import com.geovannycode.ecommerce.catalog.domain.model.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class JpaProductRepository implements ProductRepository {

    private final SpringDataProductRepository springDataProductRepository;

    public JpaProductRepository(SpringDataProductRepository springDataProductRepository) {
        this.springDataProductRepository = springDataProductRepository;
    }

    @Override
    public Optional<Product> findByCode(String code) {
        return springDataProductRepository.findByCode(code);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return springDataProductRepository.findAll(pageable);
    }
}
