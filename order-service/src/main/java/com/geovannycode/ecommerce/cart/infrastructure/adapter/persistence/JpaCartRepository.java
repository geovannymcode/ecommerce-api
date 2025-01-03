package com.geovannycode.ecommerce.cart.infrastructure.adapter.persistence;

import com.geovannycode.ecommerce.cart.application.ports.output.CartRepository;
import com.geovannycode.ecommerce.cart.domain.model.Cart;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCartRepository implements CartRepository {

    private final SpringDataCartRepository springDataCatRepository;

    public JpaCartRepository(SpringDataCartRepository springDataCatRepository) {
        this.springDataCatRepository = springDataCatRepository;
    }

    @Override
    public Cart save(Cart cart) {
        return springDataCatRepository.save(cart);
    }

    @Override
    public Optional<Cart> findById(String cartId) {
        return springDataCatRepository.findById(cartId);
    }

    @Override
    public void deleteById(String cartId) {
        springDataCatRepository.deleteById(cartId);
    }
}
