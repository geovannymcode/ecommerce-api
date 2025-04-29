package com.geovannycode.bookstore.webapp.infrastructure.api.controller;

import com.geovannycode.bookstore.webapp.domain.model.CreateOrderRequest;
import com.geovannycode.bookstore.webapp.domain.model.OrderConfirmationDTO;
import com.geovannycode.bookstore.webapp.domain.model.OrderDTO;
import com.geovannycode.bookstore.webapp.domain.model.OrderSummary;
import com.geovannycode.bookstore.webapp.infrastructure.clients.orders.OrderServiceClient;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderServiceClient orderServiceClient;

    public OrderController(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    @PostMapping
    public OrderConfirmationDTO createOrder(@Valid @RequestBody CreateOrderRequest orderRequest) {
        log.info("Creating order with request: {}", orderRequest);
        return orderServiceClient.createOrder(orderRequest);
    }

    @GetMapping("/{orderNumber}")
    public OrderDTO getOrder(@PathVariable String orderNumber) {
        log.info("Getting order details for order number: {}", orderNumber);
        return orderServiceClient.getOrder(orderNumber);
    }

    @GetMapping
    public List<OrderSummary> getOrders() {
        log.info("Getting all orders");
        return orderServiceClient.getOrders();
    }
}
