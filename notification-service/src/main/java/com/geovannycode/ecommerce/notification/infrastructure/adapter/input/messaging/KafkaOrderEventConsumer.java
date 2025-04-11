package com.geovannycode.ecommerce.notification.infrastructure.adapter.input.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geovannycode.ecommerce.notification.domain.model.OrderCancelledEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderCreatedEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderDeliveredEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderErrorEvent;
import com.geovannycode.ecommerce.notification.domain.port.input.NotificationUseCase;
import com.geovannycode.ecommerce.notification.domain.port.output.OrderEventRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaOrderEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaOrderEventConsumer.class);

    private final NotificationUseCase notificationUseCase;
    private final OrderEventRepositoryPort orderEventRepository;
    private final ObjectMapper objectMapper;

    public KafkaOrderEventConsumer(
            NotificationUseCase notificationUseCase,
            OrderEventRepositoryPort orderEventRepository,
            ObjectMapper objectMapper) {
        this.notificationUseCase = notificationUseCase;
        this.orderEventRepository = orderEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.topic.order-created}")
    public void handleOrderCreatedEvent(String message) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            log.info("Received OrderCreatedEvent: {}", event);

            if (!orderEventRepository.existsByEventId(event.eventId())) {
                notificationUseCase.sendOrderCreatedNotification(event);
            } else {
                log.info("Event already processed: {}", event.eventId());
            }
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.order-delivered}")
    public void handleOrderDeliveredEvent(String message) {
        try {
            OrderDeliveredEvent event = objectMapper.readValue(message, OrderDeliveredEvent.class);
            log.info("Received OrderDeliveredEvent: {}", event);

            if (!orderEventRepository.existsByEventId(event.eventId())) {
                notificationUseCase.sendOrderDeliveredNotification(event);
            } else {
                log.info("Event already processed: {}", event.eventId());
            }
        } catch (Exception e) {
            log.error("Error processing OrderDeliveredEvent", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.order-cancelled}")
    public void handleOrderCancelledEvent(String message) {
        try {
            OrderCancelledEvent event = objectMapper.readValue(message, OrderCancelledEvent.class);
            log.info("Received OrderCancelledEvent: {}", event);

            if (!orderEventRepository.existsByEventId(event.eventId())) {
                notificationUseCase.sendOrderCancelledNotification(event);
            } else {
                log.info("Event already processed: {}", event.eventId());
            }
        } catch (Exception e) {
            log.error("Error processing OrderCancelledEvent", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.order-error}")
    public void handleOrderErrorEvent(String message) {
        try {
            OrderErrorEvent event = objectMapper.readValue(message, OrderErrorEvent.class);
            log.info("Received OrderErrorEvent: {}", event);

            if (!orderEventRepository.existsByEventId(event.eventId())) {
                notificationUseCase.sendOrderErrorEventNotification(event);
            } else {
                log.info("Event already processed: {}", event.eventId());
            }
        } catch (Exception e) {
            log.error("Error processing OrderErrorEvent", e);
        }
    }
}
