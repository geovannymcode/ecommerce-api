package com.geovannycode.ecommerce.notification.domain.model;

public interface OrderEvent {
    String getEventId();

    String getOrderNumber();
}
