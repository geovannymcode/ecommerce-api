package com.geovannycode.ecommerce.payment.application.ports.output;

import com.geovannycode.ecommerce.payment.domain.model.CreditCard;
import java.util.Optional;

public interface CreditCardRepository {
    Optional<CreditCard> findByCardNumber(String cardNumber);
}
