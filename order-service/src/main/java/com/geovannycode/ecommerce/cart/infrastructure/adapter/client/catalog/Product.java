package com.geovannycode.ecommerce.cart.infrastructure.adapter.client.catalog;

import java.math.BigDecimal;

public record Product(
        String id,
        String code,
        String name,
        String description,
        String imageUrl,
        BigDecimal price,
        BigDecimal discount,
        BigDecimal salePrice
) {
}