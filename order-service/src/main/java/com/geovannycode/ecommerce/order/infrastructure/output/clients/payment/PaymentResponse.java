package com.geovannycode.ecommerce.order.infrastructure.output.clients.payment;

public record PaymentResponse(PaymentStatus status) {
    public enum PaymentStatus {
        ACCEPTED,
        REJECTED
    }
}
