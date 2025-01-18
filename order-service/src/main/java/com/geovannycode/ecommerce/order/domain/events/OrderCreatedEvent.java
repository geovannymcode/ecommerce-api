package com.geovannycode.ecommerce.order.domain.events;

import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderItemEntity;
import com.geovannycode.ecommerce.order.domain.model.Address;
import com.geovannycode.ecommerce.order.domain.model.Customer;

import java.time.LocalDateTime;
import java.util.Set;

public record OrderCreatedEvent(
        String eventId,
        String orderNumber,
        Set<OrderItemEntity> items,
        Customer customer,
        Address deliveryAddress,
        LocalDateTime createdAt) {}
