package com.geovannycode.ecommerce.cart.domain.exception;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String message) {
        super(message);
    }

    public static CartNotFoundException forCart(String cartId) {
        return new CartNotFoundException("Cart for cartId " + cartId + " not found");
    }
}
