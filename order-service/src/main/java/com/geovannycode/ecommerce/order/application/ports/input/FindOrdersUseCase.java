package com.geovannycode.ecommerce.order.application.ports.input;

import com.geovannycode.ecommerce.order.application.dto.OrderSummary;
import java.util.List;

public interface FindOrdersUseCase {
    List<OrderSummary> findOrders(String userName);
}
