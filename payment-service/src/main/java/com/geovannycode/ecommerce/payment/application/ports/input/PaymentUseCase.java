package com.geovannycode.ecommerce.payment.application.ports.input;

import com.geovannycode.ecommerce.payment.application.dto.PaymentRequest;
import com.geovannycode.ecommerce.payment.application.dto.PaymentResponse;

public interface PaymentUseCase {
    PaymentResponse validate(PaymentRequest request);
}
