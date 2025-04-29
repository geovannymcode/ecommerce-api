package com.geovannycode.bookstore.webapp.domain.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class Cart {
    private String cartId;
    private Set<CartItem> items;
    private BigDecimal totalAmount;

    public Cart() {
        this.items = new HashSet<>();
    }

    public Cart(String cartId) {
        this.cartId = cartId;
        this.items = new HashSet<>();
    }

    public Cart(String cartId, Set<CartItem> items) {
        this.cartId = cartId;
        this.items = items != null ? new HashSet<>(items) : new HashSet<>();
    }

    public BigDecimal calculateTotal() {
        return items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
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
