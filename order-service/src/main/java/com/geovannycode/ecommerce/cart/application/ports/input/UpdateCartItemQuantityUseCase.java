package com.geovannycode.ecommerce.cart.application.ports.input;

import com.geovannycode.ecommerce.cart.domain.model.Cart;
import com.geovannycode.ecommerce.cart.application.dto.CartItemRequestDTO;

public interface UpdateCartItemQuantityUseCase {
    Cart updateCartItemQuantity(String cartId, CartItemRequestDTO cartItemRequest);
}
