package com.geovannycode.ecommerce.notification.infrastructure.persistence.repository;

import com.geovannycode.ecommerce.notification.infrastructure.persistence.entity.OrderEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataOrderEventRepository extends JpaRepository<OrderEventEntity, Long> {
    boolean existsByEventId(String eventId);
}
