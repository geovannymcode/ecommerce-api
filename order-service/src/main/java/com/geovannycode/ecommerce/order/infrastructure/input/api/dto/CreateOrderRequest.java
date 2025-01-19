package com.geovannycode.ecommerce.order.infrastructure.input.api.dto;

import com.geovannycode.ecommerce.order.domain.model.Address;
import com.geovannycode.ecommerce.order.domain.model.Customer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CreateOrderRequest(
        @Valid @NotEmpty(message = "Items cannot be empty") Set<OrderItemDTO> items,
        @Valid Customer customer,
        @Valid Address deliveryAddress) {}
