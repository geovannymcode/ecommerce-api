package com.geovannycode.ecommerce.notification.infrastructure.adapter.output.persistence;

import com.geovannycode.ecommerce.notification.domain.port.output.OrderEventRepositoryPort;
import com.geovannycode.ecommerce.notification.infrastructure.persistence.entity.OrderEventEntity;
import com.geovannycode.ecommerce.notification.infrastructure.persistence.repository.SpringDataOrderEventRepository;
import org.springframework.stereotype.Component;

@Component
public class JpaOrderEventRepository implements OrderEventRepositoryPort {

    private final SpringDataOrderEventRepository repository;

    public JpaOrderEventRepository(SpringDataOrderEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return repository.existsByEventId(eventId);
    }

    @Override
    public void save(String eventId) {
        OrderEventEntity entity = new OrderEventEntity(eventId);
        repository.save(entity);
    }
}
