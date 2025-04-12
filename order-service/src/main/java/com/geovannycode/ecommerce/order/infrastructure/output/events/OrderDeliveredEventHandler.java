package com.geovannycode.ecommerce.order.infrastructure.output.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geovannycode.ecommerce.order.application.service.OrderService;
import com.geovannycode.ecommerce.order.common.model.OrderDTO;
import com.geovannycode.ecommerce.order.common.model.OrderDeliveredEvent;
import com.geovannycode.ecommerce.order.common.model.enums.OrderStatus;
import org.slf4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderDeliveredEventHandler {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(OrderDeliveredEventHandler.class);
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public OrderDeliveredEventHandler(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.delivered-orders-topic}", groupId = "orders")
    public void handle(String payload) {
        try {
            OrderDeliveredEvent event = objectMapper.readValue(payload, OrderDeliveredEvent.class);
            log.info("Received a OrderDeliveredEvent with orderId:{}: ", event.getOrderNumber());
            OrderDTO order =
                    orderService.findOrderByOrderId(event.getOrderNumber()).orElse(null);
            if (order == null) {
                log.info("Received invalid OrderDeliveredEvent with orderId:{}: ", event.getOrderNumber());
                return;
            }
            orderService.updateOrderStatus(order.orderNumber(), OrderStatus.DELIVERED, null);
        } catch (RuntimeException | JsonProcessingException e) {
            log.error("Error processing OrderDeliveredEvent. Payload: {}", payload);
            log.error(e.getMessage(), e);
        }
    }
}
