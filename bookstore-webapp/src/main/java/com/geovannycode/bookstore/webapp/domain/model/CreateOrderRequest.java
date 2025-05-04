package com.geovannycode.bookstore.webapp.domain.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public class CreateOrderRequest {
    @NotEmpty(message = "Items cannot be empty")
    private Set<OrderItem> items;

    @Valid
    private Customer customer;

    @Valid
    private Address deliveryAddress;

    // Constructor vacío (importante para frameworks)
    public CreateOrderRequest() {}

    public CreateOrderRequest(Set<OrderItem> items, Customer customer, Address deliveryAddress) {
        this.items = items;
        this.customer = customer;
        this.deliveryAddress = deliveryAddress;
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
}

