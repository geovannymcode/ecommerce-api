package com.geovannycode.ecommerce.order.infrastructure.output.repository;

import com.geovannycode.ecommerce.order.application.ports.output.OrderStatusHistoryRepository;
import com.geovannycode.ecommerce.order.common.model.OrderStatusHistory;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderStatusHistoryEntity;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class JpaOrderStatusHistoryRepository implements OrderStatusHistoryRepository {

    private final SpringDataOrderStatusHistoryRepository springDataOrderStatusHistoryRepository;

    public JpaOrderStatusHistoryRepository(
            SpringDataOrderStatusHistoryRepository springDataOrderStatusHistoryRepository) {
        this.springDataOrderStatusHistoryRepository = springDataOrderStatusHistoryRepository;
    }

    @Override
    public void save(OrderStatusHistory orderStatusHistory) {
        OrderStatusHistoryEntity entity = mapToEntity(orderStatusHistory);
        springDataOrderStatusHistoryRepository.save(entity);
    }

    @Override
    public List<OrderStatusHistory> findByOrderNumber(String orderNumber) {
        return springDataOrderStatusHistoryRepository.findByOrderNumberOrderByChangedAtDesc(orderNumber).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private OrderStatusHistoryEntity mapToEntity(OrderStatusHistory domain) {
        OrderStatusHistoryEntity entity = new OrderStatusHistoryEntity();
        entity.setOrderNumber(domain.getOrderNumber());
        entity.setPreviousStatus(domain.getPreviousStatus());
        entity.setNewStatus(domain.getNewStatus());
        entity.setComments(domain.getComments());
        entity.setChangedAt(domain.getChangedAt());
        entity.setChangedBy(domain.getChangedBy());
        return entity;
    }

    private OrderStatusHistory mapToDomain(OrderStatusHistoryEntity entity) {
        return new OrderStatusHistory(
                entity.getOrderNumber(),
                entity.getPreviousStatus(),
                entity.getNewStatus(),
                entity.getComments(),
                entity.getChangedAt(),
                entity.getChangedBy());
    }
}
