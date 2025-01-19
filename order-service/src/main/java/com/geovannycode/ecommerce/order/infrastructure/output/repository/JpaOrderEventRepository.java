package com.geovannycode.ecommerce.order.infrastructure.output.repository;

import com.geovannycode.ecommerce.order.application.ports.output.OrderEventRepository;
import com.geovannycode.ecommerce.order.domain.events.OrderCancelledEvent;
import com.geovannycode.ecommerce.order.domain.events.OrderCreatedEvent;
import com.geovannycode.ecommerce.order.domain.events.OrderDeliveredEvent;
import com.geovannycode.ecommerce.order.domain.events.OrderErrorEvent;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEventEntity;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import com.geovannycode.ecommerce.order.domain.model.enums.OrderEventType;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaOrderEventRepository implements OrderEventRepository {

    private final SpringDataOrderEventRepository springDataOrderEventRepository;

    public JpaOrderEventRepository(SpringDataOrderEventRepository springDataOrderEventRepository) {
        this.springDataOrderEventRepository = springDataOrderEventRepository;
    }


    @Override
    public void save(OrderEventEntity orderEventEntity) {
        springDataOrderEventRepository.save(orderEventEntity);
    }

    @Override
    public List<OrderEventEntity> findAll(Sort sort) {
        return springDataOrderEventRepository.findAll(sort);
    }

    @Override
    public void delete(OrderEventEntity orderEventEntity) {
        springDataOrderEventRepository.delete(orderEventEntity);
    }

    @Override
    public Optional<OrderEventEntity> findById(Long id) {
        return springDataOrderEventRepository.findById(id);
    }

}
