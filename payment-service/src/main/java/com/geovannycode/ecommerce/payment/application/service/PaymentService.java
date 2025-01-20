package com.geovannycode.ecommerce.payment.application.service;

import com.geovannycode.ecommerce.payment.application.dto.PaymentRequest;
import com.geovannycode.ecommerce.payment.application.dto.PaymentResponse;
import com.geovannycode.ecommerce.payment.application.ports.input.PaymentUseCase;
import com.geovannycode.ecommerce.payment.application.ports.output.CreditCardRepository;
import com.geovannycode.ecommerce.payment.domain.model.CreditCard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PaymentService implements PaymentUseCase {
    private final CreditCardRepository creditCardRepository;

    public PaymentService(CreditCardRepository creditCardRepository) {
        this.creditCardRepository = creditCardRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse validate(PaymentRequest request) {
        Optional<CreditCard> creditCardOptional =
                creditCardRepository.findByCardNumber(request.cardNumber());
        if (creditCardOptional.isEmpty()) {
            return PaymentResponse.REJECTED;
        }
        CreditCard creditCard = creditCardOptional.get();
        if (creditCard.cvv().equals(request.cvv())
                && creditCard.expiryMonth() == request.expiryMonth()
                && creditCard.expiryYear() == request.expiryYear()) {
            return PaymentResponse.ACCEPTED;
        }
        return PaymentResponse.REJECTED;
    }
}
