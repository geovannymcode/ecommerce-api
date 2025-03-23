package com.geovannycode.ecommerce.order.common.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record CreateOrderRequest(
        @Valid @NotEmpty(message = "Items cannot be empty") Set<OrderItem> items,
        @Valid Customer customer,
        @Valid Address deliveryAddress,
        @Valid Payment payment) {

    public record Payment(
            @NotBlank(message = "Card Number is required") String cardNumber,
            @NotBlank(message = "CVV is required") String cvv,
            @NotNull Integer expiryMonth,
            @NotNull Integer expiryYear) {}
}
