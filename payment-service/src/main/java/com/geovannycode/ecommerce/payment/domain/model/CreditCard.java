package com.geovannycode.ecommerce.payment.domain.model;

public record CreditCard(
        Long id, String customerName, String cardNumber, String cvv, int expiryMonth, int expiryYear) {}
