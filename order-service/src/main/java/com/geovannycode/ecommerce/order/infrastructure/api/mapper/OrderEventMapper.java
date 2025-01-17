package com.geovannycode.ecommerce.order.infrastructure.api.mapper;

import com.geovannycode.ecommerce.order.domain.model.Order;
import com.geovannycode.ecommerce.order.domain.model.OrderItem;
import com.geovannycode.ecommerce.order.infrastructure.api.dto.OrderCancelledEvent;
import com.geovannycode.ecommerce.order.infrastructure.api.dto.OrderCreatedEvent;
import com.geovannycode.ecommerce.order.infrastructure.api.dto.OrderDeliveredEvent;
import com.geovannycode.ecommerce.order.infrastructure.api.dto.OrderErrorEvent;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

class OrderEventMapper {

    static OrderCreatedEvent buildOrderCreatedEvent(Order order) {
        return new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                order.getOrderNumber(),
                getOrderItems(order),
                order.getCustomer(),
                order.getDeliveryAddress(),
                LocalDateTime.now());
    }

    static OrderDeliveredEvent buildOrderDeliveredEvent(Order order) {
        return new OrderDeliveredEvent(
                UUID.randomUUID().toString(),
                order.getOrderNumber(),
                getOrderItems(order),
                order.getCustomer(),
                order.getDeliveryAddress(),
                LocalDateTime.now());
    }

    static OrderCancelledEvent buildOrderCancelledEvent(Order order, String reason) {
        return new OrderCancelledEvent(
                UUID.randomUUID().toString(),
                order.getOrderNumber(),
                getOrderItems(order),
                order.getCustomer(),
                order.getDeliveryAddress(),
                reason,
                LocalDateTime.now());
    }

    static OrderErrorEvent buildOrderErrorEvent(Order order, String reason) {
        return new OrderErrorEvent(
                UUID.randomUUID().toString(),
                order.getOrderNumber(),
                getOrderItems(order),
                order.getCustomer(),
                order.getDeliveryAddress(),
                reason,
                LocalDateTime.now());
    }

    private static Set<OrderItem> getOrderItems(Order order) {
        return order.getItems().stream()
                .map(item -> new OrderItem(item.getCode(), item.getName(), item.getPrice(), item.getQuantity()))
                .collect(Collectors.toSet());
    }
}
