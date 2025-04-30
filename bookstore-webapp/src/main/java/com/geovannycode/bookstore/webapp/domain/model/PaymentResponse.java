package com.geovannycode.bookstore.webapp.domain.model;

import com.geovannycode.bookstore.webapp.domain.model.enums.PaymentStatus;

public class PaymentResponse {
    private PaymentStatus status;

    public static final PaymentResponse ACCEPTED = new PaymentResponse(PaymentStatus.ACCEPTED);
    public static final PaymentResponse REJECTED = new PaymentResponse(PaymentStatus.REJECTED);

    public PaymentResponse() {}

    public PaymentResponse(PaymentStatus status) {
        this.status = status;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}
