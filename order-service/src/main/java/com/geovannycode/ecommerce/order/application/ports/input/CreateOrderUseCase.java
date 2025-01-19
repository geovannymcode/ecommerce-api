package com.geovannycode.ecommerce.order.application.ports.input;

import com.geovannycode.ecommerce.order.infrastructure.input.api.dto.CreateOrderRequest;
import com.geovannycode.ecommerce.order.infrastructure.input.api.dto.CreateOrderResponse;

public interface CreateOrderUseCase {
    CreateOrderResponse createOrder(String userName, CreateOrderRequest request);
}
