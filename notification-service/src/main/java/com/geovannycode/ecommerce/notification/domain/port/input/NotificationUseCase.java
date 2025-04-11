package com.geovannycode.ecommerce.notification.domain.port.input;

import com.geovannycode.ecommerce.notification.domain.model.OrderCancelledEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderCreatedEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderDeliveredEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderErrorEvent;

public interface NotificationUseCase {
    void sendOrderCreatedNotification(OrderCreatedEvent event);
    void sendOrderDeliveredNotification(OrderDeliveredEvent event);
    void sendOrderCancelledNotification(OrderCancelledEvent event);
    void sendOrderErrorEventNotification(OrderErrorEvent event);
}
