package com.geovannycode.bookstore.webapp.domain.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public class CartItemRequestDTO {
    @NotEmpty
    private String code;

    @Min(0)
    private int quantity;

    public CartItemRequestDTO() {}

    public CartItemRequestDTO(String code, int quantity) {
        this.code = code;
        this.quantity = quantity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
