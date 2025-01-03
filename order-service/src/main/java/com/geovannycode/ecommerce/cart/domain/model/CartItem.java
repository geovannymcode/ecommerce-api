package com.geovannycode.ecommerce.cart.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItem {

    private String code;
    private String name;
    private String description;
    private BigDecimal price;
    private int quantity;

    public BigDecimal getSubTotal() {
        return price.multiply(new BigDecimal(quantity));
    }
}
