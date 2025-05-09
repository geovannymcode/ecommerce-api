package com.geovannycode.ecommerce.order.application.ports.output;

import com.geovannycode.ecommerce.order.application.dto.OrderSummary;
import com.geovannycode.ecommerce.order.common.model.enums.OrderStatus;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    List<OrderEntity> findOrderByStatus(OrderStatus status);

    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    void updateOrderStatus(String orderNumber, OrderStatus status);

    List<OrderSummary> findByUserName(String userName);

    Optional<OrderEntity> findByUserNameAndOrderNumber(String userName, String orderNumber);

    OrderEntity save(OrderEntity orderEntity);

    boolean existsByOrderNumber(String orderNumber);
}
