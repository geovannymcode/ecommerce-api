package com.geovannycode.ecommerce.order.infrastructure.input.api.mapper;

import com.geovannycode.ecommerce.order.domain.model.enums.OrderStatus;
import com.geovannycode.ecommerce.order.infrastructure.input.api.dto.CreateOrderRequest;
import com.geovannycode.ecommerce.order.infrastructure.input.api.dto.OrderDTO;
import com.geovannycode.ecommerce.order.infrastructure.input.api.dto.OrderItemDTO;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderItemEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderEntity convertToEntity(CreateOrderRequest request) {
        OrderEntity newOrder = new OrderEntity();
        newOrder.setOrderNumber(UUID.randomUUID().toString());
        newOrder.setStatus(OrderStatus.NEW);
        newOrder.setCustomer(request.customer());
        newOrder.setDeliveryAddress(request.deliveryAddress());
        Set<OrderItemEntity> orderItems = new HashSet<>();
        for (OrderItemDTO item : request.items()) {
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setCode(item.code());
            orderItem.setName(item.name());
            orderItem.setPrice(item.price());
            orderItem.setQuantity(item.quantity());
            orderItem.setOrder(newOrder);
            orderItems.add(orderItem);
        }
        newOrder.setItems(orderItems);
        return newOrder;
    }

    public static OrderDTO convertToDTO(OrderEntity order) {
        Set<OrderItemDTO> orderItems = order.getItems().stream()
                .map(item -> new OrderItemDTO(item.getCode(), item.getName(), item.getPrice(), item.getQuantity()))
                .collect(Collectors.toSet());

        return new OrderDTO(
                order.getOrderNumber(),
                order.getUserName(),
                orderItems,
                order.getCustomer(),
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getComments(),
                order.getCreatedAt());
    }
}