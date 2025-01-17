package com.geovannycode.ecommerce.order.infrastructure.api.exception;

public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
