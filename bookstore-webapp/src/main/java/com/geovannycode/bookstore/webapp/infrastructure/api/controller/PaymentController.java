package com.geovannycode.bookstore.webapp.infrastructure.api.controller;

import com.geovannycode.bookstore.webapp.domain.model.PaymentRequest;
import com.geovannycode.bookstore.webapp.domain.model.PaymentResponse;
import com.geovannycode.bookstore.webapp.infrastructure.clients.payment.PaymentServiceClient;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentServiceClient paymentServiceClient;

    public PaymentController(PaymentServiceClient paymentServiceClient) {
        this.paymentServiceClient = paymentServiceClient;
    }

    @PostMapping("/validate")
    public PaymentResponse validate(@Valid @RequestBody PaymentRequest paymentRequest) {
        log.info("Validating payment request: {}", paymentRequest);
        return paymentServiceClient.validate(paymentRequest);
    }
}
