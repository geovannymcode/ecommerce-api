package com.geovannycode.ecommerce.order.infrastructure.output.repository;

import com.geovannycode.ecommerce.order.application.dto.OrderSummary;
import com.geovannycode.ecommerce.order.application.ports.output.OrderRepository;
import com.geovannycode.ecommerce.order.domain.model.enums.OrderStatus;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaOrderRepository implements OrderRepository {
    private final SpringDataOrderRepository springDataOrderRepository;

    public JpaOrderRepository(SpringDataOrderRepository springDataOrderRepository) {
        this.springDataOrderRepository = springDataOrderRepository;
    }

    @Override
    public List<OrderEntity> findByStatus(OrderStatus status) {
        return springDataOrderRepository.findByStatus(status);
    }

    @Override
    public Optional<OrderEntity> findByOrderNumber(String orderNumber) {
        return springDataOrderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public void updateOrderStatus(String orderNumber, OrderStatus status) {
        OrderEntity order =
                springDataOrderRepository.findByOrderNumber(orderNumber).orElseThrow();
        order.setStatus(status);
        springDataOrderRepository.save(order);
    }

    @Override
    public List<OrderSummary> findByUserName(String userName) {
        return springDataOrderRepository.findByUserName(userName);
    }

    @Override
    public Optional<OrderEntity> findByUserNameAndOrderNumber(String userName, String orderNumber) {
        return springDataOrderRepository.findByUserNameAndOrderNumber(userName, orderNumber);
    }

    @Override
    public OrderEntity save(OrderEntity orderEntity) {
        return null;
    }
}
