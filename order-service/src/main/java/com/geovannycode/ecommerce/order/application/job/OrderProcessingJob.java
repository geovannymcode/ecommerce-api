package com.geovannycode.ecommerce.order.application.job;

import com.geovannycode.ecommerce.order.application.ports.output.OrderEventRepository;
import com.geovannycode.ecommerce.order.application.service.OrderEventService;
import com.geovannycode.ecommerce.order.application.service.OrderService;
import com.geovannycode.ecommerce.order.common.model.Address;
import com.geovannycode.ecommerce.order.common.model.Customer;
import com.geovannycode.ecommerce.order.common.model.OrderCancelledEvent;
import com.geovannycode.ecommerce.order.common.model.OrderDeliveredEvent;
import com.geovannycode.ecommerce.order.common.model.OrderErrorEvent;
import com.geovannycode.ecommerce.order.common.model.OrderItem;
import com.geovannycode.ecommerce.order.common.model.enums.OrderEventType;
import com.geovannycode.ecommerce.order.common.model.enums.OrderStatus;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private final OrderEventService orderEventService;
    private final OrderEventRepository orderEventRepository;

    public OrderProcessingJob(
            OrderService orderService, OrderEventService orderEventService, OrderEventRepository orderEventRepository) {
        this.orderService = orderService;
        this.orderEventService = orderEventService;
        this.orderEventRepository = orderEventRepository;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processNewOrders() {
        List<OrderEntity> newOrders = orderService.findOrdersByStatus(OrderStatus.NEW);
        log.info("Found {} new orders to process", newOrders.size());

        for (OrderEntity order : newOrders) {
            orderService.updateOrderStatus(order.getOrderNumber(), OrderStatus.IN_PROCESS, "Order in process");
            log.info("Published OrderCreatedEvent for orderId:{}", order.getOrderNumber());
        }
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processPaymentRejectedOrders() {
        List<OrderEntity> orders = orderService.findOrdersByStatus(OrderStatus.PAYMENT_REJECTED);
        log.info("Found {} payment rejected orders to process", orders.size());
        for (OrderEntity order : orders) {
            orderService.updateOrderStatus(order.getOrderNumber(), OrderStatus.ERROR, "Payment rejected");

            if (!hasRecentEvent(order.getOrderNumber(), OrderEventType.ORDER_PROCESSING_FAILED)) {
                OrderErrorEvent errorEvent = buildOrderErrorEvent(order, "Payment rejected");
                orderEventService.save(errorEvent);
                log.info("Published OrderErrorEvent for orderId:{}", order.getOrderNumber());
            } else {
                log.info("Skipped publishing duplicate OrderErrorEvent for orderId:{}", order.getOrderNumber());
            }
        }
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
        return new Customer(entityCustomer.getName(), entityCustomer.getEmail(), entityCustomer.getPhone());
    }

    private Address getDeliveryAddress(OrderEntity order) {
        Address entityAddress = order.getDeliveryAddress();
        return new Address(
                entityAddress.getAddressLine1(),
                entityAddress.getAddressLine2(),
                entityAddress.getCity(),
                entityAddress.getState(),
                entityAddress.getZipCode(),
                entityAddress.getCountry());
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processInProcessOrders() {
        List<OrderEntity> inProcessOrders = orderService.findOrdersByStatus(OrderStatus.IN_PROCESS);
        log.info("Found {} in-process orders to process", inProcessOrders.size());

        for (OrderEntity order : inProcessOrders) {
            try {
                if (canDeliverToCountry(order.getDeliveryAddress().getCountry())) {
                    log.info("OrderNumber: {} can be delivered", order.getOrderNumber());
                    orderService.updateOrderStatus(
                            order.getOrderNumber(), OrderStatus.DELIVERED, "Order delivered successfully");

                    if (!hasRecentEvent(order.getOrderNumber(), OrderEventType.ORDER_DELIVERED)) {
                        OrderDeliveredEvent deliveredEvent = new OrderDeliveredEvent(
                                UUID.randomUUID().toString(),
                                order.getOrderNumber(),
                                getOrderItems(order),
                                getCustomer(order),
                                getDeliveryAddress(order),
                                LocalDateTime.now());
                        orderEventService.save(deliveredEvent);
                        log.info("Published OrderDeliveredEvent for orderId:{}", order.getOrderNumber());
                    } else {
                        log.info(
                                "Skipped publishing duplicate OrderDeliveredEvent for orderId:{}",
                                order.getOrderNumber());
                    }
                } else {
                    log.info("OrderNumber: {} cannot be delivered", order.getOrderNumber());
                    orderService.updateOrderStatus(
                            order.getOrderNumber(), OrderStatus.CANCELLED, "Cannot deliver to this location");

                    if (!hasRecentEvent(order.getOrderNumber(), OrderEventType.ORDER_CANCELLED)) {
                        OrderCancelledEvent cancelledEvent = new OrderCancelledEvent(
                                UUID.randomUUID().toString(),
                                order.getOrderNumber(),
                                getOrderItems(order),
                                getCustomer(order),
                                getDeliveryAddress(order),
                                "Cannot deliver to this location",
                                LocalDateTime.now());
                        orderEventService.save(cancelledEvent);
                        log.info("Published OrderCancelledEvent for orderId:{}", order.getOrderNumber());
                    } else {
                        log.info(
                                "Skipped publishing duplicate OrderCancelledEvent for orderId:{}",
                                order.getOrderNumber());
                    }
                }
            } catch (Exception e) {
                log.error("Error processing order {}: {}", order.getOrderNumber(), e.getMessage(), e);
                orderService.updateOrderStatus(
                        order.getOrderNumber(), OrderStatus.ERROR, "Processing error: " + e.getMessage());

                if (!hasRecentEvent(order.getOrderNumber(), OrderEventType.ORDER_PROCESSING_FAILED)) {
                    OrderErrorEvent errorEvent = new OrderErrorEvent(
                            UUID.randomUUID().toString(),
                            order.getOrderNumber(),
                            getOrderItems(order),
                            getCustomer(order),
                            getDeliveryAddress(order),
                            "Processing error: " + e.getMessage(),
                            LocalDateTime.now());
                    orderEventService.save(errorEvent);
                    log.info("Published OrderErrorEvent for orderId:{}", order.getOrderNumber());
                } else {
                    log.info("Skipped publishing duplicate OrderErrorEvent for orderId:{}", order.getOrderNumber());
                }
            }
        }
    }

    private boolean hasRecentEvent(String orderNumber, OrderEventType eventType) {
        LocalDateTime cutoffTime = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
        return orderEventRepository.existsByOrderNumberAndEventTypeAndCreatedAtAfter(
                orderNumber, eventType, cutoffTime);
    }

    private boolean canDeliverToCountry(String country) {
        List<String> allowedCountries = List.of("BRASIL", "INDIA", "USA", "GERMANY", "COLOMBIA");
        return allowedCountries.contains(country.toUpperCase());
    }
}
