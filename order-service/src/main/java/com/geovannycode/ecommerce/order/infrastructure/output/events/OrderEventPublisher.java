package com.geovannycode.ecommerce.order.infrastructure.output.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geovannycode.ecommerce.order.ApplicationProperties;
import com.geovannycode.ecommerce.order.application.ports.output.EventPublisherPort;
import com.geovannycode.ecommerce.order.common.model.OrderCancelledEvent;
import com.geovannycode.ecommerce.order.common.model.OrderCreatedEvent;
import com.geovannycode.ecommerce.order.common.model.OrderDeliveredEvent;
import com.geovannycode.ecommerce.order.common.model.OrderErrorEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher implements EventPublisherPort {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationProperties properties;
    private final ObjectMapper objectMapper;

    OrderEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate, ApplicationProperties properties, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public void publish(OrderCreatedEvent event) {
        this.send(properties.newOrdersTopic(), event);
    }

    public void publish(OrderDeliveredEvent event) {
        this.send(properties.deliveredOrdersTopic(), event);
    }

    public void publish(OrderCancelledEvent event) {
        this.send(properties.cancelledOrdersTopic(), event);
    }

    public void publish(OrderErrorEvent event) {
        this.send(properties.errorOrdersTopic(), event);
    }

    private void send(String topic, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, jsonPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing event", e);
        }
    }
}
