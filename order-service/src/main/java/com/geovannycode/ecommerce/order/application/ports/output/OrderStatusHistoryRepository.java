package com.geovannycode.ecommerce.order.application.ports.output;

import com.geovannycode.ecommerce.order.common.model.OrderStatusHistory;
import java.util.List;

public interface OrderStatusHistoryRepository {
    void save(OrderStatusHistory orderStatusHistory);

    List<OrderStatusHistory> findByOrderNumber(String orderNumber);
}
