package com.geovannycode.ecommerce.order.infrastructure.output.events.mapper;

import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderItemEntity;
import com.geovannycode.ecommerce.order.domain.events.OrderCancelledEvent;
import com.geovannycode.ecommerce.order.domain.events.OrderCreatedEvent;
import com.geovannycode.ecommerce.order.domain.events.OrderDeliveredEvent;
import com.geovannycode.ecommerce.order.domain.events.OrderErrorEvent;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

class OrderEventMapper {

    static OrderCreatedEvent buildOrderCreatedEvent(OrderEntity orderEntity) {
        return new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                orderEntity.getOrderNumber(),
                getOrderItems(orderEntity),
                orderEntity.getCustomer(),
                orderEntity.getDeliveryAddress(),
                LocalDateTime.now());
    }

    static OrderDeliveredEvent buildOrderDeliveredEvent(OrderEntity orderEntity) {
        return new OrderDeliveredEvent(
                UUID.randomUUID().toString(),
                orderEntity.getOrderNumber(),
                getOrderItems(orderEntity),
                orderEntity.getCustomer(),
                orderEntity.getDeliveryAddress(),
                LocalDateTime.now());
    }

    static OrderCancelledEvent buildOrderCancelledEvent(OrderEntity orderEntity, String reason) {
        return new OrderCancelledEvent(
                UUID.randomUUID().toString(),
                orderEntity.getOrderNumber(),
                getOrderItems(orderEntity),
                orderEntity.getCustomer(),
                orderEntity.getDeliveryAddress(),
                reason,
                LocalDateTime.now());
    }

    static OrderErrorEvent buildOrderErrorEvent(OrderEntity orderEntity, String reason) {
        return new OrderErrorEvent(
                UUID.randomUUID().toString(),
                orderEntity.getOrderNumber(),
                getOrderItems(orderEntity),
                orderEntity.getCustomer(),
                orderEntity.getDeliveryAddress(),
                reason,
                LocalDateTime.now());
    }

    private static Set<OrderItemEntity> getOrderItems(OrderEntity orderEntity) {
        return orderEntity.getItems().stream()
                .map(item -> new OrderItemEntity(item.getCode(), item.getName(), item.getPrice(), item.getQuantity()))
                .collect(Collectors.toSet());
    }
}
