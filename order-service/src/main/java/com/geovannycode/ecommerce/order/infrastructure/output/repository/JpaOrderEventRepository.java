package com.geovannycode.ecommerce.order.infrastructure.output.repository;

import com.geovannycode.ecommerce.order.application.ports.output.OrderEventRepository;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEventEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class JpaOrderEventRepository implements OrderEventRepository {

    private final SpringDataOrderEventRepository springDataOrderEventRepository;

    public JpaOrderEventRepository(SpringDataOrderEventRepository springDataOrderEventRepository) {
        this.springDataOrderEventRepository = springDataOrderEventRepository;
    }

    @Override
    public void save(OrderEventEntity event) {
        springDataOrderEventRepository.save(event);
    }

    @Override
    public List<OrderEventEntity> findAll(Sort sort) {
        return springDataOrderEventRepository.findAll(sort);
    }

    @Override
    public void delete(OrderEventEntity event) {
        springDataOrderEventRepository.delete(event);
    }

    @Override
    public Optional<OrderEventEntity> findById(Long id) {
        return springDataOrderEventRepository.findById(id);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return springDataOrderEventRepository.existsByEventId(eventId);
    }
}
