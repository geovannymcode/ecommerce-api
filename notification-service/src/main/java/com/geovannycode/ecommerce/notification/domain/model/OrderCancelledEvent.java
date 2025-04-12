package com.geovannycode.ecommerce.notification.domain.model;

import java.time.LocalDateTime;
import java.util.Set;

public class OrderCancelledEvent {
    private String eventId;
    private String orderNumber;
    private Set<OrderItem> items;
    private Customer customer;
    private Address deliveryAddress;
    private String reason;
    private LocalDateTime createdAt;

    public OrderCancelledEvent() {}

    public OrderCancelledEvent(
            String eventId,
            String orderNumber,
            Set<OrderItem> items,
            Customer customer,
            Address deliveryAddress,
            String reason,
            LocalDateTime createdAt) {
        this.eventId = eventId;
        this.orderNumber = orderNumber;
        this.items = items;
        this.customer = customer;
        this.deliveryAddress = deliveryAddress;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Set<OrderItem> getItems() {
        return items;
    }

    public void setItems(Set<OrderItem> items) {
        this.items = items;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
