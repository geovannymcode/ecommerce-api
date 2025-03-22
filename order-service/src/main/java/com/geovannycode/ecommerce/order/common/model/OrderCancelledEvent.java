package com.geovannycode.ecommerce.order.common.model;

import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderItemEntity;

import java.time.LocalDateTime;
import java.util.Set;

public record OrderCancelledEvent(
        String eventId,
        String orderNumber,
        Set<OrderItem> items,
        Customer customer,
        Address deliveryAddress,
        String reason,
        LocalDateTime createdAt) {}
