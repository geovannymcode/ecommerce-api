package com.geovannycode.ecommerce.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geovannycode.ecommerce.order.application.ports.output.EventPublisherPort;
import com.geovannycode.ecommerce.order.application.ports.output.OrderEventRepository;
import com.geovannycode.ecommerce.order.application.ports.output.OrderRepository;
import com.geovannycode.ecommerce.order.common.model.OrderCancelledEvent;
import com.geovannycode.ecommerce.order.common.model.OrderCreatedEvent;
import com.geovannycode.ecommerce.order.common.model.OrderDeliveredEvent;
import com.geovannycode.ecommerce.order.common.model.OrderErrorEvent;
import com.geovannycode.ecommerce.order.common.model.enums.OrderEventType;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEventEntity;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderEventService {
    private static final Logger log = LoggerFactory.getLogger(OrderEventService.class);

    private final OrderEventRepository orderEventRepository;
    private final EventPublisherPort eventPublisher;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    public OrderEventService(
            OrderEventRepository orderEventRepository,
            EventPublisherPort eventPublisher,
            ObjectMapper objectMapper,
            OrderRepository orderRepository) {
        this.orderEventRepository = orderEventRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
    }

    public void save(OrderCreatedEvent event) {
        try {
            log.info("Saving OrderCreatedEvent with ID: {} for order: {}", event.getEventId(), event.getOrderNumber());
            boolean eventExists = orderEventRepository.existsByEventId(event.getEventId());

            if (eventExists) {
                log.info("Event with ID {} already exists, skipping", event.getEventId());
                return;
            }

            boolean orderExists = orderRepository.existsByOrderNumber(event.getOrderNumber());
            if (!orderExists) {
                log.warn(
                        "Cannot save OrderCreatedEvent: Order with number {} does not exist in the database",
                        event.getOrderNumber());
                return;
            }

            OrderEventEntity orderEvent = new OrderEventEntity();
            orderEvent.setEventId(event.getEventId());
            orderEvent.setEventType(OrderEventType.ORDER_CREATED);
            orderEvent.setOrderNumber(event.getOrderNumber());
            orderEvent.setCreatedAt(event.getCreatedAt());
            orderEvent.setPayload(toJsonPayload(event));

            log.info("Order exists check for orderNumber {}: {}", event.getOrderNumber(), orderExists);

            this.orderEventRepository.save(orderEvent);
            log.info("Successfully saved OrderEventEntity with ID: {}", orderEvent.getEventId());

        } catch (Exception e) {
            log.error("Error saving OrderCreatedEvent: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void save(OrderDeliveredEvent event) {
        OrderEventEntity orderEvent = new OrderEventEntity();
        orderEvent.setEventId(event.getEventId());
        orderEvent.setEventType(OrderEventType.ORDER_DELIVERED);
        orderEvent.setOrderNumber(event.getOrderNumber());
        orderEvent.setCreatedAt(event.getCreatedAt());
        orderEvent.setPayload(toJsonPayload(event));
        this.orderEventRepository.save(orderEvent);
    }

    public void save(OrderCancelledEvent event) {
        OrderEventEntity orderEvent = new OrderEventEntity();
        orderEvent.setEventId(event.getEventId());
        orderEvent.setEventType(OrderEventType.ORDER_CANCELLED);
        orderEvent.setOrderNumber(event.getOrderNumber());
        orderEvent.setCreatedAt(event.getCreatedAt());
        orderEvent.setPayload(toJsonPayload(event));
        this.orderEventRepository.save(orderEvent);
    }

    public void save(OrderErrorEvent event) {
        OrderEventEntity orderEvent = new OrderEventEntity();
        orderEvent.setEventId(event.getEventId());
        orderEvent.setEventType(OrderEventType.ORDER_PROCESSING_FAILED);
        orderEvent.setOrderNumber(event.getOrderNumber());
        orderEvent.setCreatedAt(event.getCreatedAt());
        orderEvent.setPayload(toJsonPayload(event));
        this.orderEventRepository.save(orderEvent);
    }

    private void publishEvent(OrderEventEntity event) {
        OrderEventType eventType = event.getEventType();
        switch (eventType) {
            case ORDER_CREATED:
                OrderCreatedEvent orderCreatedEvent = fromJsonPayload(event.getPayload(), OrderCreatedEvent.class);
                eventPublisher.publish(orderCreatedEvent);
                break;
            case ORDER_DELIVERED:
                OrderDeliveredEvent orderDeliveredEvent =
                        fromJsonPayload(event.getPayload(), OrderDeliveredEvent.class);
                eventPublisher.publish(orderDeliveredEvent);
                break;
            case ORDER_CANCELLED:
                OrderCancelledEvent orderCancelledEvent =
                        fromJsonPayload(event.getPayload(), OrderCancelledEvent.class);
                eventPublisher.publish(orderCancelledEvent);
                break;
            case ORDER_PROCESSING_FAILED:
                OrderErrorEvent orderErrorEvent = fromJsonPayload(event.getPayload(), OrderErrorEvent.class);
                eventPublisher.publish(orderErrorEvent);
                break;
            default:
                log.warn("Unsupported OrderEventType: {}", eventType);
        }
    }

    private String toJsonPayload(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T fromJsonPayload(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void schedulePublishOrderEvents() {
        log.info("Starting scheduled publication of order events");

        Sort sort = Sort.by("createdAt").ascending();
        List<OrderEventEntity> events = orderEventRepository.findAll(sort);
        log.info("Found {} Order Events to be published", events.size());

        for (OrderEventEntity event : events) {
            try {
                log.info(
                        "Publishing event: type={}, orderNumber={}, eventId={}",
                        event.getEventType(),
                        event.getOrderNumber(),
                        event.getEventId());

                this.publishEvent(event);

                log.info("Successfully published event: {}", event.getEventId());
                // orderEventRepository.delete(event);
            } catch (Exception e) {
                log.error("Error publishing event {}: {}", event.getEventId(), e.getMessage(), e);
            }
        }

        log.info("Completed scheduled publication of order events");
    }
}
