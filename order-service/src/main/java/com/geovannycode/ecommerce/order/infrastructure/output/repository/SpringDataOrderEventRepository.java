package com.geovannycode.ecommerce.order.infrastructure.output.repository;

import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderEventRepository extends JpaRepository<OrderEventEntity, Long> {}
