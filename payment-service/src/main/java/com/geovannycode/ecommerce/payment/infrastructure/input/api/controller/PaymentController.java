package com.geovannycode.ecommerce.payment.infrastructure.input.api.controller;

import com.geovannycode.ecommerce.payment.application.dto.PaymentRequest;
import com.geovannycode.ecommerce.payment.application.dto.PaymentResponse;
import com.geovannycode.ecommerce.payment.application.ports.input.PaymentUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentUseCase paymentUseCase;

    public PaymentController(PaymentUseCase paymentUseCase) {
        this.paymentUseCase = paymentUseCase;
    }

    @PostMapping("/validate")
    public PaymentResponse validate(@Valid @RequestBody PaymentRequest paymentRequest) {
        return paymentUseCase.validate(paymentRequest);
    }
}