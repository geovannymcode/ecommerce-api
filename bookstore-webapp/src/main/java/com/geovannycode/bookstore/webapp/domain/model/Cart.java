package com.geovannycode.bookstore.webapp.domain.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class Cart {
    private String id;
    private Set<CartItem> items;
    private BigDecimal totalAmount;

    public Cart() {
        this.items = new HashSet<>();
    }

    public Cart(String id) {
        this.id = id;
        this.items = new HashSet<>();
    }

    public Cart(String id, Set<CartItem> items) {
        this.id = id;
        this.items = items != null ? new HashSet<>(items) : new HashSet<>();
    }

    public BigDecimal calculateTotal() {
        return items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<CartItem> getItems() {
        return items;
    }

    public void setItems(Set<CartItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        if (totalAmount == null) {
            totalAmount = calculateTotal();
        }
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
