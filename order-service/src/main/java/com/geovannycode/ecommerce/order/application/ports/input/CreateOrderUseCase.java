package com.geovannycode.ecommerce.order.application.ports.input;

import com.geovannycode.ecommerce.order.common.model.CreateOrderRequest;
import com.geovannycode.ecommerce.order.common.model.CreateOrderResponse;

public interface CreateOrderUseCase {
    CreateOrderResponse createOrder(String userName, CreateOrderRequest request);
}
