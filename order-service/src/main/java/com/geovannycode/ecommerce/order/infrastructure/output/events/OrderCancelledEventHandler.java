package com.geovannycode.ecommerce.order.infrastructure.output.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geovannycode.ecommerce.order.application.service.OrderService;
import com.geovannycode.ecommerce.order.common.model.OrderCancelledEvent;
import com.geovannycode.ecommerce.order.common.model.OrderDTO;
import com.geovannycode.ecommerce.order.common.model.enums.OrderStatus;
import org.slf4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCancelledEventHandler {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(OrderCancelledEventHandler.class);
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public OrderCancelledEventHandler(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.cancelled-orders-topic}", groupId = "orders")
    public void handle(String payload) {
        try {
            OrderCancelledEvent event = objectMapper.readValue(payload, OrderCancelledEvent.class);
            log.info("Received a OrderCancelledEvent with orderId:{}: ", event.getOrderNumber());
            OrderDTO order =
                    orderService.findOrderByOrderId(event.getOrderNumber()).orElse(null);
            if (order == null) {
                log.info("Received invalid OrderCancelledEvent with orderId:{}: ", event.getOrderNumber());
                return;
            }
            orderService.updateOrderStatus(event.getOrderNumber(), OrderStatus.CANCELLED, event.getReason());
        } catch (JsonProcessingException e) {
            log.error("Error processing OrderCancelledEvent. Payload: {}", payload);
            log.error(e.getMessage(), e);
        }
    }
}
