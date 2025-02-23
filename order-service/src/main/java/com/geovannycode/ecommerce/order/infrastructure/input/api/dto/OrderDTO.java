package com.geovannycode.ecommerce.order.infrastructure.input.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.geovannycode.ecommerce.order.domain.model.Address;
import com.geovannycode.ecommerce.order.domain.model.Customer;
import com.geovannycode.ecommerce.order.domain.model.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record OrderDTO(
        String orderNumber,
        String user,
        Set<OrderItemDTO> items,
        Customer customer,
        Address deliveryAddress,
        OrderStatus status,
        String comments,
        LocalDateTime createdAt) {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
