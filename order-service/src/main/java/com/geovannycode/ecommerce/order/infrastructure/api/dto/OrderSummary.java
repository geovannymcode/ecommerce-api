package com.geovannycode.ecommerce.order.infrastructure.api.dto;

import com.geovannycode.ecommerce.order.domain.model.enums.OrderStatus;

public record OrderSummary(String orderNumber, OrderStatus status) {}
