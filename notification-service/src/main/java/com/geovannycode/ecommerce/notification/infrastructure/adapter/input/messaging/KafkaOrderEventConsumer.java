package com.geovannycode.ecommerce.notification.infrastructure.adapter.input.messaging;

import com.geovannycode.ecommerce.notification.domain.model.OrderCancelledEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderCreatedEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderDeliveredEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderErrorEvent;
import com.geovannycode.ecommerce.notification.domain.port.input.NotificationUseCase;
import com.geovannycode.ecommerce.notification.domain.port.output.OrderEventRepositoryPort;
import com.geovannycode.ecommerce.notification.infrastructure.adapter.input.messaging.mapper.KafkaEventMapper;
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
    private final KafkaEventMapper kafkaEventMapper;

    public KafkaOrderEventConsumer(
            NotificationUseCase notificationService,
            OrderEventRepositoryPort orderEventRepository,
            KafkaEventMapper kafkaEventMapper) {
        this.notificationService = notificationService;
        this.orderEventRepository = orderEventRepository;
        this.kafkaEventMapper = kafkaEventMapper;
    }

    @KafkaListener(topics = "${notification.order-created-topic}")
    public void handleOrderCreatedEvent(String message) {
        try {
            log.info("Received OrderCreatedEvent message");

            OrderCreatedEvent event = kafkaEventMapper.mapToOrderCreatedEvent(message);

            if (orderEventRepository.existsByEventId(event.getEventId())) {
                log.warn("Received duplicate OrderCreatedEvent with eventId: {}", event.getEventId());
                return;
            }

            log.info("Processing OrderCreatedEvent with orderNumber: {}", event.getOrderNumber());
            notificationService.sendOrderCreatedNotification(event);

            log.info("Received raw OrderCreatedEvent message: {}", message);
            orderEventRepository.save(event.getEventId());

            log.info("Successfully processed OrderCreatedEvent with orderNumber: {}", event.getOrderNumber());

        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "${notification.order-delivered-topic}")
    public void handleOrderDeliveredEvent(String message) {
        try {
            log.info("Received OrderDeliveredEvent message");

            // Usar el mapper para convertir el mensaje a un evento de dominio
            OrderDeliveredEvent event = kafkaEventMapper.mapToOrderDeliveredEvent(message);

            // Verificar si ya hemos procesado este evento
            if (orderEventRepository.existsByEventId(event.getEventId())) {
                log.warn("Received duplicate OrderDeliveredEvent with eventId: {}", event.getEventId());
                return;
            }

            log.info("Processing OrderDeliveredEvent with orderNumber: {}", event.getOrderNumber());

            // Enviar la notificación
            notificationService.sendOrderDeliveredNotification(event);

            // Guardar el evento para evitar duplicados
            orderEventRepository.save(event.getEventId());

            log.info("Successfully processed OrderDeliveredEvent with orderNumber: {}", event.getOrderNumber());

        } catch (Exception e) {
            log.error("Error processing OrderDeliveredEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "${notification.order-cancelled-topic}")
    public void handleOrderCancelledEvent(String message) {
        try {
            log.info("Received OrderCancelledEvent message");

            // Usar el mapper para convertir el mensaje a un evento de dominio
            OrderCancelledEvent event = kafkaEventMapper.mapToOrderCancelledEvent(message);

            // Verificar si ya hemos procesado este evento
            if (orderEventRepository.existsByEventId(event.getEventId())) {
                log.warn("Received duplicate OrderCancelledEvent with eventId: {}", event.getEventId());
                return;
            }

            log.info("Processing OrderCancelledEvent with orderNumber: {}", event.getOrderNumber());

            // Enviar la notificación
            notificationService.sendOrderCancelledNotification(event);

            // Guardar el evento para evitar duplicados
            orderEventRepository.save(event.getEventId());

            log.info("Successfully processed OrderCancelledEvent with orderNumber: {}", event.getOrderNumber());

        } catch (Exception e) {
            log.error("Error processing OrderCancelledEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "${notification.order-error-topic}")
    public void handleOrderErrorEvent(String message) {
        try {
            log.info("Received OrderErrorEvent message");

            // Usar el mapper para convertir el mensaje a un evento de dominio
            OrderErrorEvent event = kafkaEventMapper.mapToOrderErrorEvent(message);

            // Verificar si ya hemos procesado este evento
            if (orderEventRepository.existsByEventId(event.getEventId())) {
                log.warn("Received duplicate OrderErrorEvent with eventId: {}", event.getEventId());
                return;
            }

            log.info("Processing OrderErrorEvent with orderNumber: {}", event.getOrderNumber());

            // Enviar la notificación
            notificationService.sendOrderErrorEventNotification(event);

            // Guardar el evento para evitar duplicados
            orderEventRepository.save(event.getEventId());

            log.info("Successfully processed OrderErrorEvent with orderNumber: {}", event.getOrderNumber());

        } catch (Exception e) {
            log.error("Error processing OrderErrorEvent: {}", e.getMessage(), e);
        }
    }
}
