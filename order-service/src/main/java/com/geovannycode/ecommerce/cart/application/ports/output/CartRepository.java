package com.geovannycode.ecommerce.cart.application.ports.output;

import com.geovannycode.ecommerce.cart.domain.model.Cart;
import java.util.Optional;

public interface CartRepository {
    Cart save(Cart cart);

    Optional<Cart> findById(String cartId);

    void deleteById(String cartId);
}
