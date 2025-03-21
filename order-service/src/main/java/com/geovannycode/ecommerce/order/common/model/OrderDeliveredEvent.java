package com.geovannycode.ecommerce.order.common.model;

import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderItemEntity;

import java.util.Set;

public record OrderDeliveredEvent(
        String orderId, Set<OrderItemEntity> items, Customer customer, Address deliveryAddress) {}
