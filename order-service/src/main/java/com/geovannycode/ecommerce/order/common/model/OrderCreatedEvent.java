package com.geovannycode.ecommerce.order.common.model;

import java.time.LocalDateTime;
import java.util.Set;

public class OrderCreatedEvent {

    private String eventId;
    private String orderNumber;
    private Set<OrderItem> items;
    private Customer customer;
    private Address deliveryAddress;
    private LocalDateTime createdAt;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(
            String eventId,
            String orderNumber,
            Set<OrderItem> items,
            Customer customer,
            Address deliveryAddress,
            LocalDateTime createdAt) {
        this.eventId = eventId;
        this.orderNumber = orderNumber;
        this.items = items;
        this.customer = customer;
        this.deliveryAddress = deliveryAddress;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
