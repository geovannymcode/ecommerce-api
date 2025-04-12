package com.geovannycode.ecommerce.order.application.ports.input;

import com.geovannycode.ecommerce.order.common.model.enums.OrderStatus;

public interface UpdateOrderStatusUseCase {
    void updateOrderStatus(String orderNumber, OrderStatus status, String comments);

    void updateOrderStatus(String orderNumber, OrderStatus status, String comments, String changedBy);
}
