package com.geovannycode.ecommerce.notification.domain.model;

import java.math.BigDecimal;

public class OrderItem {
    private String code;
    private String name;
    private BigDecimal price;
    private Integer quantity;

    public OrderItem() {}

    public OrderItem(String code, String name, BigDecimal price, Integer quantity) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
