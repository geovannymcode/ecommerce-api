package com.geovannycode.ecommerce.cart.infrastructure.output.repository;

import com.geovannycode.ecommerce.cart.application.ports.output.CartRepository;
import com.geovannycode.ecommerce.cart.domain.model.Cart;
import java.util.Optional;
import org.springframework.stereotype.Repository;

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
