package com.geovannycode.ecommerce.order.infrastructure.api.dto;

import com.geovannycode.ecommerce.order.domain.model.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CreateOrderRequest(
        @Valid @NotEmpty(message = "Items cannot be empty") Set<OrderItem> items,
        @Valid Customer customer,
        @Valid Address deliveryAddress) {}
