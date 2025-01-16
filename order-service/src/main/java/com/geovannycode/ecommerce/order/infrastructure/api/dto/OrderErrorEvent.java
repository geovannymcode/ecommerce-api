package com.geovannycode.ecommerce.order.infrastructure.api.dto;

import com.geovannycode.ecommerce.order.domain.model.OrderItem;

import java.time.LocalDateTime;
import java.util.Set;

public record OrderErrorEvent(
        String eventId,
        String orderNumber,
        Set<OrderItem> items,
        Customer customer,
        Address deliveryAddress,
        String reason,
        LocalDateTime createdAt) {}
