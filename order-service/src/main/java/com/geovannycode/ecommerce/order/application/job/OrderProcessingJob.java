package com.geovannycode.ecommerce.order.application.job;

import com.geovannycode.ecommerce.order.ApplicationProperties;
import com.geovannycode.ecommerce.order.application.service.OrderService;
import com.geovannycode.ecommerce.order.domain.events.OrderCreatedEvent;
import com.geovannycode.ecommerce.order.domain.events.OrderErrorEvent;
import com.geovannycode.ecommerce.order.domain.model.Address;
import com.geovannycode.ecommerce.order.domain.model.Customer;
import com.geovannycode.ecommerce.order.domain.model.enums.OrderStatus;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderItemEntity;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrderProcessingJob {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(OrderProcessingJob.class);
    private final OrderService orderService;
    private final KafkaHelper kafkaHelper;
    private final ApplicationProperties properties;

    public OrderProcessingJob(
            OrderService orderService, KafkaHelper kafkaHelper, ApplicationProperties properties) {
        this.orderService = orderService;
        this.kafkaHelper = kafkaHelper;
        this.properties = properties;
    }

    @Scheduled(fixedDelay = 60000)
    public void processNewOrders() {
        List<OrderEntity> newOrders = orderService.findOrdersByStatus(OrderStatus.NEW);
        for (OrderEntity order : newOrders) {
            OrderCreatedEvent orderCreatedEvent = this.buildOrderCreatedEvent(order);
            kafkaHelper.send(properties.newOrdersTopic(), orderCreatedEvent);
            log.info("Published OrderCreatedEvent for orderId:{}", order.getOrderId());
            orderService.updateOrderStatus(order.getOrderId(), OrderStatus.IN_PROCESS, null);
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void processPaymentRejectedOrders() {
        List<OrderEntity> orders = orderService.findOrdersByStatus(OrderStatus.PAYMENT_REJECTED);
        for (OrderEntity order : orders) {
            OrderErrorEvent orderErrorEvent = this.buildOrderErrorEvent(order, "Payment rejected");
            kafkaHelper.send(properties.errorOrdersTopic(), orderErrorEvent);
            log.info("Published OrderErrorEvent for orderId:{}", order.getOrderId());
        }
    }

    private OrderCreatedEvent buildOrderCreatedEvent(OrderEntity order) {
        return new OrderCreatedEvent(
                order.getOrderId(),
                getOrderItems(order),
                getCustomer(order),
                getDeliveryAddress(order));
    }

    private OrderErrorEvent buildOrderErrorEvent(OrderEntity order, String reason) {
        return new OrderErrorEvent(
                order.getOrderId(),
                reason,
                getOrderItems(order),
                getCustomer(order),
                getDeliveryAddress(order));
    }

    private Set<OrderItemEntity> getOrderItems(OrderEntity order) {
        return order.getItems().stream()
                .map(
                        item ->
                                new OrderItemEntity(
                                        item.getCode(),
                                        item.getName(),
                                        item.getPrice(),
                                        item.getQuantity()))
                .collect(Collectors.toSet());
    }

    private Customer getCustomer(OrderEntity order) {
        return new Customer(
                order.getCustomerName(), order.getCustomerEmail(), order.getCustomerPhone());
    }

    private Address getDeliveryAddress(OrderEntity order) {
        return new Address(
                order.getDeliveryAddressLine1(),
                order.getDeliveryAddressLine2(),
                order.getDeliveryAddressCity(),
                order.getDeliveryAddressState(),
                order.getDeliveryAddressZipCode(),
                order.getDeliveryAddressCountry());
    }
}
