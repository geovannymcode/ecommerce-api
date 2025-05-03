package com.geovannycode.ecommerce.cart.domain.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItem {

    private String code;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private int quantity;

    public BigDecimal getSubTotal() {
        return price.multiply(new BigDecimal(quantity));
    }
}
