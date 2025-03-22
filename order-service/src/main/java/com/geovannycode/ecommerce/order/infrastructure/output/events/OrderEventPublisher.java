package com.geovannycode.ecommerce.order.infrastructure.output.events;

import com.geovannycode.ecommerce.order.ApplicationProperties;
import com.geovannycode.ecommerce.order.common.model.OrderCancelledEvent;
import com.geovannycode.ecommerce.order.common.model.OrderCreatedEvent;
import com.geovannycode.ecommerce.order.common.model.OrderDeliveredEvent;
import com.geovannycode.ecommerce.order.common.model.OrderErrorEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationProperties properties;

    OrderEventPublisher(RabbitTemplate rabbitTemplate, ApplicationProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
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

    private void send(String routingKey, Object payload) {
        rabbitTemplate.convertAndSend(properties.orderEventsExchange(), routingKey, payload);
    }
}
