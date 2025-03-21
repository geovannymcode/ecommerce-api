package com.geovannycode.ecommerce.order.infrastructure.input.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record CreateOrderRequest(
        @Valid @NotEmpty(message = "Items cannot be empty") Set<OrderItemDTO> items,
        @Valid Customer customer,
        @Valid Address deliveryAddress) {}
