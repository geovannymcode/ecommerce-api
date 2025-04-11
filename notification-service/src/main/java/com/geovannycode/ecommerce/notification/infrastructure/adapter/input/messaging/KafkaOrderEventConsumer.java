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
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class KafkaOrderEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaOrderEventConsumer.class);

    private final NotificationUseCase notificationService;
    private final OrderEventRepositoryPort orderEventRepository;
    private final ObjectMapper objectMapper;

    public KafkaOrderEventConsumer(
            NotificationUseCase notificationService,
            OrderEventRepositoryPort orderEventRepository,
            ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.orderEventRepository = orderEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${notification.order-created-topic}")
    public void handleOrderCreatedEvent(String message) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);

            if (orderEventRepository.existsByEventId(event.getEventId())) {
                log.warn("Received duplicate OrderCreatedEvent with eventId: {}", event.getEventId());
                return;
            }

            log.info("Received a OrderCreatedEvent with orderNumber:{}: ", event.getOrderNumber());
            notificationService.sendOrderCreatedNotification(event);

            // Si necesitas guardar el evento, pero has mencionado que no quieres usar save
            // Aquí puedes añadir lógica específica si es necesario
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent", e);
        }
    }

    @KafkaListener(topics = "${notification.order-delivered-topic}")
    public void handleOrderDeliveredEvent(String message) {
        try {
            OrderDeliveredEvent event = objectMapper.readValue(message, OrderDeliveredEvent.class);

            if (orderEventRepository.existsByEventId(event.getEventId())) {
                log.warn("Received duplicate OrderDeliveredEvent with eventId: {}", event.getEventId());
                return;
            }

            log.info("Received a OrderDeliveredEvent with orderNumber:{}: ", event.getOrderNumber());
            notificationService.sendOrderDeliveredNotification(event);

        } catch (Exception e) {
            log.error("Error processing OrderDeliveredEvent", e);
        }
    }

    @KafkaListener(topics = "${notification.order-cancelled-topic}")
    public void handleOrderCancelledEvent(String message) {
        try {
            OrderCancelledEvent event = objectMapper.readValue(message, OrderCancelledEvent.class);

            if (orderEventRepository.existsByEventId(event.getEventId())) {
                log.warn("Received duplicate OrderCancelledEvent with eventId: {}", event.getEventId());
                return;
            }

            log.info("Received a OrderCancelledEvent with orderNumber:{}: ", event.getOrderNumber());
            notificationService.sendOrderCancelledNotification(event);

        } catch (Exception e) {
            log.error("Error processing OrderCancelledEvent", e);
        }
    }

    @KafkaListener(topics = "${notification.order-error-topic}")
    public void handleOrderErrorEvent(String message) {
        try {
            OrderErrorEvent event = objectMapper.readValue(message, OrderErrorEvent.class);

            if (orderEventRepository.existsByEventId(event.getEventId())) {
                log.warn("Received duplicate OrderErrorEvent with eventId: {}", event.getEventId());
                return;
            }

            log.info("Received a OrderErrorEvent with orderNumber:{}: ", event.getOrderNumber());
            notificationService.sendOrderErrorEventNotification(event);

        } catch (Exception e) {
            log.error("Error processing OrderErrorEvent", e);
        }
    }
}
