package com.geovannycode.ecommerce.order.infrastructure.output.repository;

import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderStatusHistoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderStatusHistoryRepository extends JpaRepository<OrderStatusHistoryEntity, Long> {
    List<OrderStatusHistoryEntity> findByOrderNumberOrderByChangedAtDesc(String orderNumber);
}
