package com.geovannycode.ecommerce.cart.application.ports.input;

import com.geovannycode.ecommerce.cart.application.dto.CartItemRequestDTO;
import com.geovannycode.ecommerce.cart.domain.model.Cart;

public interface AddItemToCartUseCase {
    Cart addToCart(String cartId, CartItemRequestDTO cartItemRequest);
}
