package com.geovannycode.ecommerce.cart.application.ports.input;

import com.geovannycode.ecommerce.cart.domain.model.Cart;

public interface GetCartUseCase {
    Cart getCart(String cartId);
}
