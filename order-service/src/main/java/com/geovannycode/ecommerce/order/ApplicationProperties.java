package com.geovannycode.ecommerce.order;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "orders")
public record ApplicationProperties(String catalogServiceUrl) {}
