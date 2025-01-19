package com.geovannycode.ecommerce.order.application.ports.output;

import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEventEntity;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface OrderEventRepository {
    void save(OrderEventEntity orderEventEntity);
    List<OrderEventEntity> findAll(Sort sort);
    void delete(OrderEventEntity orderEventEntity);
    Optional<OrderEventEntity> findById(Long id);
}
