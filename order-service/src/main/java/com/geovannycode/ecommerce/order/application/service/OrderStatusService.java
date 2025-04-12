package com.geovannycode.ecommerce.order.application.service;

import com.geovannycode.ecommerce.order.application.ports.input.FindOrderStatusHistoryUseCase;
import com.geovannycode.ecommerce.order.application.ports.input.UpdateOrderStatusUseCase;
import com.geovannycode.ecommerce.order.application.ports.output.OrderRepository;
import com.geovannycode.ecommerce.order.application.ports.output.OrderStatusHistoryRepository;
import com.geovannycode.ecommerce.order.common.model.OrderStatusHistory;
import com.geovannycode.ecommerce.order.common.model.enums.OrderStatus;
import com.geovannycode.ecommerce.order.domain.exception.OrderNotFoundException;
import com.geovannycode.ecommerce.order.infrastructure.output.events.mapper.OrderEventMapper;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderStatusService implements UpdateOrderStatusUseCase, FindOrderStatusHistoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusService.class);

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderEventService orderEventService;

    public OrderStatusService(
            OrderRepository orderRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository,
            OrderEventService orderEventService) {
        this.orderRepository = orderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.orderEventService = orderEventService;
    }

    public void updateOrderStatus(String orderNumber, OrderStatus newStatus, String comments, String changedBy) {
        log.info("Updating order status: orderNumber={}, newStatus={}", orderNumber, newStatus);

        OrderEntity order = orderRepository.findByOrderNumber(orderNumber).orElseThrow(() -> {
            log.error("Order not found: {}", orderNumber);
            return new OrderNotFoundException(orderNumber);
        });

        OrderStatus previousStatus = order.getStatus();

        // No realizar cambios si el estado es el mismo
        if (previousStatus == newStatus) {
            log.info("Order already in status {}, no changes needed", newStatus);
            return;
        }

        // Actualizar el estado de la orden
        order.setStatus(newStatus);
        if (comments != null) {
            order.setComments(comments);
        }
        orderRepository.save(order);
        log.info(
                "Order status updated: orderNumber={}, previousStatus={}, newStatus={}",
                orderNumber,
                previousStatus,
                newStatus);

        // Registrar en el historial
        OrderStatusHistory historyEntry = new OrderStatusHistory(
                orderNumber,
                previousStatus,
                newStatus,
                comments,
                LocalDateTime.now(),
                changedBy != null ? changedBy : "SYSTEM");
        orderStatusHistoryRepository.save(historyEntry);
        log.info("Order status history recorded for orderNumber={}", orderNumber);

        // Crear y guardar eventos para estados específicos
        try {
            switch (newStatus) {
                case DELIVERED:
                    orderEventService.save(OrderEventMapper.buildOrderDeliveredEvent(order));
                    log.info("Order delivered event created for orderNumber={}", orderNumber);
                    break;
                case CANCELLED:
                    orderEventService.save(OrderEventMapper.buildOrderCancelledEvent(order, comments));
                    log.info("Order cancelled event created for orderNumber={}", orderNumber);
                    break;
                case ERROR:
                    orderEventService.save(OrderEventMapper.buildOrderErrorEvent(order, comments));
                    log.info("Order error event created for orderNumber={}", orderNumber);
                    break;
                default:
                    log.debug("No specific event created for status: {}", newStatus);
            }
        } catch (Exception e) {
            log.error("Failed to create order event for status update: {}", e.getMessage(), e);
        }
    }

    public void updateOrderStatus(String orderNumber, OrderStatus newStatus, String comments) {
        updateOrderStatus(orderNumber, newStatus, comments, null);
    }

    @Override
    public List<OrderStatusHistory> findOrderStatusHistory(String orderNumber) {
        if (!orderRepository.existsByOrderNumber(orderNumber)) {
            throw new OrderNotFoundException(orderNumber);
        }
        return orderStatusHistoryRepository.findByOrderNumber(orderNumber);
    }
}
