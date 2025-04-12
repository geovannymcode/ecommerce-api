package com.geovannycode.ecommerce.order.application.ports.input;

import com.geovannycode.ecommerce.order.common.model.OrderStatusHistory;
import java.util.List;

public interface FindOrderStatusHistoryUseCase {
    List<OrderStatusHistory> findOrderStatusHistory(String orderNumber);
}
