package com.geovannycode.ecommerce.order.application.ports.output;

import com.geovannycode.ecommerce.order.common.model.OrderCancelledEvent;
import com.geovannycode.ecommerce.order.common.model.OrderCreatedEvent;
import com.geovannycode.ecommerce.order.common.model.OrderDeliveredEvent;
import com.geovannycode.ecommerce.order.common.model.OrderErrorEvent;

public interface EventPublisherPort {
    void publish(OrderCreatedEvent event);

    void publish(OrderDeliveredEvent event);

    void publish(OrderCancelledEvent event);

    void publish(OrderErrorEvent event);
}
