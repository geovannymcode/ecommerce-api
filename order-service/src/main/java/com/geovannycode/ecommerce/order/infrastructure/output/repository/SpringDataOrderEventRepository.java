package com.geovannycode.ecommerce.order.infrastructure.output.repository;

import com.geovannycode.ecommerce.order.common.model.enums.OrderEventType;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEventEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderEventRepository extends JpaRepository<OrderEventEntity, Long> {
    boolean existsByEventId(String eventId);

    List<OrderEventEntity> findByPublishedFalse(Sort sort);

    boolean existsByOrderNumberAndEventTypeAndCreatedAtAfter(
            String orderNumber, OrderEventType eventType, LocalDateTime createdAt);
}
