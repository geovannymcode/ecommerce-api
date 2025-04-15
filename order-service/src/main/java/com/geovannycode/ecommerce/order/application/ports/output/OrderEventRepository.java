package com.geovannycode.ecommerce.order.application.ports.output;

import com.geovannycode.ecommerce.order.common.model.enums.OrderEventType;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEventEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;

public interface OrderEventRepository {
    void save(OrderEventEntity orderEventEntity);

    List<OrderEventEntity> findAll(Sort sort);

    void delete(OrderEventEntity orderEventEntity);

    Optional<OrderEventEntity> findById(Long id);

    boolean existsByEventId(String eventId);

    List<OrderEventEntity> findByPublishedFalse(Sort sort);

    boolean existsByOrderNumberAndEventTypeAndCreatedAtAfter(
            String orderNumber, OrderEventType eventType, LocalDateTime createdAt);
}
