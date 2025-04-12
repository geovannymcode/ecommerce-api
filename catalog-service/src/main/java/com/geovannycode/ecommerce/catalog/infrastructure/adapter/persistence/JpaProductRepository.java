package com.geovannycode.ecommerce.catalog.infrastructure.adapter.persistence;

import com.geovannycode.ecommerce.catalog.domain.model.Product;
import com.geovannycode.ecommerce.catalog.domain.port.output.ProductRepository;
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

    @Override
    public boolean existsProductByCode(String code) {
        return springDataProductRepository.existsProductByCode(code);
    }

    @Override
    public void deleteProduct(String code) {
        springDataProductRepository.deleteByCode(code);
    }

    @Override
    public Product save(Product product) {
        return springDataProductRepository.save(product);
    }

    @Override
    public Page<Product> searchProductsByCriteria(String query, Pageable pageable) {
        return springDataProductRepository.searchProductsByCriteria(query, pageable);
    }
}
