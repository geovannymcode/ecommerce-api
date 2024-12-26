package com.geovannycode.ecommerce.catalog.infrastructure.api.dto;

import java.math.BigDecimal;

public record ProductDto(
        String code, String name, String description, String imageUrl, BigDecimal price, Integer stock) {}
