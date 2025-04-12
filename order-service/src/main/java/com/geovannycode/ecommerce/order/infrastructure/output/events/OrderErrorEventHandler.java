package com.geovannycode.ecommerce.order.infrastructure.output.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geovannycode.ecommerce.order.application.service.OrderService;
import com.geovannycode.ecommerce.order.common.model.OrderDTO;
import com.geovannycode.ecommerce.order.common.model.OrderErrorEvent;
import com.geovannycode.ecommerce.order.common.model.enums.OrderStatus;
import org.slf4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderErrorEventHandler {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(OrderErrorEventHandler.class);
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public OrderErrorEventHandler(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.error-orders-topic}", groupId = "orders")
    public void handle(String payload) {
        try {
            OrderErrorEvent event = objectMapper.readValue(payload, OrderErrorEvent.class);
            log.info("Received a OrderErrorEvent with orderId:{}", event.getOrderNumber());
            OrderDTO order =
                    orderService.findOrderByOrderId(event.getOrderNumber()).orElse(null);
            if (order == null) {
                log.info("Received invalid OrderErrorEvent with orderId:{}", event.getOrderNumber());
                return;
            }
            orderService.updateOrderStatus(event.getOrderNumber(), OrderStatus.ERROR, event.getReason());
        } catch (JsonProcessingException e) {
            log.error("Error processing OrderErrorEvent. Payload: {}", payload);
            log.error(e.getMessage(), e);
        }
    }
}
