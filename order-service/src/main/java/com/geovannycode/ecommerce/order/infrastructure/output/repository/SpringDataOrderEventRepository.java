package com.geovannycode.ecommerce.order.infrastructure.output.repository;

import com.geovannycode.ecommerce.order.common.model.enums.OrderStatus;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEventEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataOrderEventRepository extends JpaRepository<OrderEventEntity, Long> {

    @Query("SELECT o FROM OrderEntity o JOIN FETCH o.items WHERE o.status = :status")
    List<OrderEntity> findOrderByStatus(OrderStatus status);
}
