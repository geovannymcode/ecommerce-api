package com.geovannycode.ecommerce.payment.application.dto;

import com.geovannycode.ecommerce.payment.domain.model.enums.PaymentStatus;

public record PaymentResponse(PaymentStatus status) {
    public static final PaymentResponse ACCEPTED = new PaymentResponse(PaymentStatus.ACCEPTED);
    public static final PaymentResponse REJECTED = new PaymentResponse(PaymentStatus.REJECTED);
}
