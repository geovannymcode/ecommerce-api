package com.geovannycode.bookstore.webapp.domain.model;

import java.math.BigDecimal;

public class CartItem {
    private String code;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private int quantity;

    public CartItem() {}

    public CartItem(String code, String name, String description, String imageUrl, BigDecimal price, int quantity) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
    }

    public BigDecimal getSubTotal() {
        return price.multiply(new BigDecimal(quantity));
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
