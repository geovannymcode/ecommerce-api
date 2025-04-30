package com.geovannycode.bookstore.webapp.infrastructure.clients.payment;

import com.geovannycode.bookstore.webapp.domain.model.PaymentRequest;
import com.geovannycode.bookstore.webapp.domain.model.PaymentResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface PaymentServiceClient {

    @PostExchange("/payments/api/payments/validate")
    PaymentResponse validate(@RequestBody PaymentRequest paymentRequest);
}
