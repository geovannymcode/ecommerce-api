package com.geovannycode.ecommerce.order.infrastructure.output.events.mapper;

import com.geovannycode.ecommerce.order.infrastructure.input.api.dto.OrderItemDTO;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import com.geovannycode.ecommerce.order.domain.events.OrderCancelledEvent;
import com.geovannycode.ecommerce.order.domain.events.OrderCreatedEvent;
import com.geovannycode.ecommerce.order.domain.events.OrderDeliveredEvent;
import com.geovannycode.ecommerce.order.domain.events.OrderErrorEvent;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderEventMapper {

    public static OrderCreatedEvent buildOrderCreatedEvent(OrderEntity order) {
        return new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                order.getOrderNumber(),
                getOrderItems(order),
                order.getCustomer(),
                order.getDeliveryAddress(),
                LocalDateTime.now());
    }

    public static OrderDeliveredEvent buildOrderDeliveredEvent(OrderEntity order) {
        return new OrderDeliveredEvent(
                UUID.randomUUID().toString(),
                order.getOrderNumber(),
                getOrderItems(order),
                order.getCustomer(),
                order.getDeliveryAddress(),
                LocalDateTime.now());
    }

    public static OrderCancelledEvent buildOrderCancelledEvent(OrderEntity order, String reason) {
        return new OrderCancelledEvent(
                UUID.randomUUID().toString(),
                order.getOrderNumber(),
                getOrderItems(order),
                order.getCustomer(),
                order.getDeliveryAddress(),
                reason,
                LocalDateTime.now());
    }

    public static OrderErrorEvent buildOrderErrorEvent(OrderEntity order, String reason) {
        return new OrderErrorEvent(
                UUID.randomUUID().toString(),
                order.getOrderNumber(),
                getOrderItems(order),
                order.getCustomer(),
                order.getDeliveryAddress(),
                reason,
                LocalDateTime.now());
    }

    private static Set<OrderItemDTO> getOrderItems(OrderEntity order) {
        return order.getItems().stream()
                .map(item -> new OrderItemDTO(item.getCode(), item.getName(), item.getPrice(), item.getQuantity()))
                .collect(Collectors.toSet());
    }
}
