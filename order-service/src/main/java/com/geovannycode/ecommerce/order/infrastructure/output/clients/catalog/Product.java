package com.geovannycode.ecommerce.order.infrastructure.output.clients.catalog;

import java.math.BigDecimal;

public record Product(String code, String name, String description, String imageUrl, BigDecimal price, Integer stock, BigDecimal discount) {}
