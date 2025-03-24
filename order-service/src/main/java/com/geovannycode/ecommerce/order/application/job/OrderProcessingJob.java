package com.geovannycode.ecommerce.order.application.job;

import com.geovannycode.ecommerce.order.application.service.OrderService;
import com.geovannycode.ecommerce.order.common.model.Address;
import com.geovannycode.ecommerce.order.common.model.Customer;
import com.geovannycode.ecommerce.order.common.model.OrderCreatedEvent;
import com.geovannycode.ecommerce.order.common.model.OrderErrorEvent;
import com.geovannycode.ecommerce.order.common.model.OrderItem;
import com.geovannycode.ecommerce.order.common.model.enums.OrderStatus;
import com.geovannycode.ecommerce.order.infrastructure.output.events.OrderEventPublisher;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderProcessingJob {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(OrderProcessingJob.class);
    private final OrderService orderService;
    private final OrderEventPublisher orderEventPublisher;

    public OrderProcessingJob(OrderService orderService, OrderEventPublisher orderEventPublisher) {
        this.orderService = orderService;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processNewOrders() {
        List<OrderEntity> newOrders = orderService.findOrdersByStatus(OrderStatus.NEW);
        for (OrderEntity order : newOrders) {
            OrderCreatedEvent orderCreatedEvent = this.buildOrderCreatedEvent(order);
            orderEventPublisher.publish(orderCreatedEvent);
            log.info("Published OrderCreatedEvent for orderId:{}", order.getOrderNumber());
            orderService.updateOrderStatus(order.getOrderNumber(), OrderStatus.IN_PROCESS, null);
        }
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processPaymentRejectedOrders() {
        List<OrderEntity> orders = orderService.findOrdersByStatus(OrderStatus.PAYMENT_REJECTED);
        for (OrderEntity order : orders) {
            OrderErrorEvent orderErrorEvent = this.buildOrderErrorEvent(order, "Payment rejected");
            orderEventPublisher.publish(orderErrorEvent);
            log.info("Published OrderErrorEvent for orderId:{}", order.getOrderNumber());
        }
    }

    private OrderCreatedEvent buildOrderCreatedEvent(OrderEntity order) {
        return new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                order.getOrderNumber(),
                getOrderItems(order),
                getCustomer(order),
                getDeliveryAddress(order),
                LocalDateTime.now());
    }

    private OrderErrorEvent buildOrderErrorEvent(OrderEntity order, String reason) {
        return new OrderErrorEvent(
                UUID.randomUUID().toString(),
                order.getOrderNumber(),
                getOrderItems(order),
                getCustomer(order),
                getDeliveryAddress(order),
                reason,
                LocalDateTime.now());
    }

    private Set<OrderItem> getOrderItems(OrderEntity order) {
        return order.getItems().stream()
                .map(item -> new OrderItem(item.getCode(), item.getName(), item.getPrice(), item.getQuantity()))
                .collect(Collectors.toSet());
    }

    private Customer getCustomer(OrderEntity order) {
        Customer entityCustomer = order.getCustomer();
        return new Customer(entityCustomer.name(), entityCustomer.email(), entityCustomer.phone());
    }

    private Address getDeliveryAddress(OrderEntity order) {
        Address entityAddress = order.getDeliveryAddress();
        return new Address(
                entityAddress.addressLine1(),
                entityAddress.addressLine2(),
                entityAddress.city(),
                entityAddress.state(),
                entityAddress.zipCode(),
                entityAddress.country());
    }
}
