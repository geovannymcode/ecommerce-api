package com.geovannycode.ecommerce.order.application.service;

import com.geovannycode.ecommerce.order.application.dto.OrderSummary;
import com.geovannycode.ecommerce.order.application.ports.input.CreateOrderUseCase;
import com.geovannycode.ecommerce.order.application.ports.input.FindOrdersUseCase;
import com.geovannycode.ecommerce.order.application.ports.input.FindUserOrderUseCase;
import com.geovannycode.ecommerce.order.application.ports.input.ProcessNewOrdersUseCase;
import com.geovannycode.ecommerce.order.application.ports.output.OrderRepository;
import com.geovannycode.ecommerce.order.common.model.CreateOrderRequest;
import com.geovannycode.ecommerce.order.common.model.CreateOrderResponse;
import com.geovannycode.ecommerce.order.common.model.OrderCreatedEvent;
import com.geovannycode.ecommerce.order.common.model.OrderDTO;
import com.geovannycode.ecommerce.order.common.model.enums.OrderStatus;
import com.geovannycode.ecommerce.order.domain.exception.OrderCancellationException;
import com.geovannycode.ecommerce.order.domain.exception.OrderNotFoundException;
import com.geovannycode.ecommerce.order.infrastructure.input.api.mapper.OrderMapper;
import com.geovannycode.ecommerce.order.infrastructure.input.api.validator.OrderValidator;
import com.geovannycode.ecommerce.order.infrastructure.output.clients.payment.PaymentRequest;
import com.geovannycode.ecommerce.order.infrastructure.output.clients.payment.PaymentResponse;
import com.geovannycode.ecommerce.order.infrastructure.output.clients.payment.PaymentServiceClient;
import com.geovannycode.ecommerce.order.infrastructure.output.events.mapper.OrderEventMapper;
import com.geovannycode.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService
        implements CreateOrderUseCase, FindOrdersUseCase, FindUserOrderUseCase, ProcessNewOrdersUseCase {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final List<String> DELIVERY_ALLOWED_COUNTRIES =
            List.of("BRASIL", "INDIA", "USA", "GERMANY", "COLOMBIA");

    private final OrderRepository orderRepository;
    private final OrderValidator orderValidator;
    private final OrderEventService orderEventService;
    private final PaymentServiceClient paymentServiceClient;

    public OrderService(
            OrderRepository orderRepository,
            OrderValidator orderValidator,
            OrderEventService orderEventService,
            PaymentServiceClient paymentServiceClient) {
        this.orderRepository = orderRepository;
        this.orderValidator = orderValidator;
        this.orderEventService = orderEventService;
        this.paymentServiceClient = paymentServiceClient;
    }

    @Override
    public CreateOrderResponse createOrder(String userName, CreateOrderRequest request) {
        orderValidator.validate(request);
        OrderEntity newOrder = OrderMapper.convertToEntity(request);
        newOrder.setUserName(userName);

        if (newOrder.getStatus() == null) {
            newOrder.setStatus(OrderStatus.NEW);
        }

        if (request.payment() != null) {
            CreateOrderRequest.Payment payment = request.payment();
            PaymentRequest paymentRequest = new PaymentRequest(
                    payment.cardNumber(), payment.cvv(), payment.expiryMonth(), payment.expiryYear());

            PaymentResponse paymentResponse = paymentServiceClient.authorize(paymentRequest);

            if (paymentResponse.status() != PaymentResponse.PaymentStatus.ACCEPTED) {
                newOrder.setStatus(OrderStatus.PAYMENT_REJECTED);
                newOrder.setComments("Payment rejected");
                OrderEntity savedOrder = this.orderRepository.save(newOrder);

                // No se crea evento para pagos rechazados ya que OrderEventService no lo soporta
                log.info("Payment rejected for orderNumber={}", savedOrder.getOrderNumber());

                return new CreateOrderResponse(savedOrder.getOrderNumber());
            }
        }

        OrderEntity savedOrder = this.orderRepository.save(newOrder);
        log.info("Created Order with orderNumber={}", savedOrder.getOrderNumber());
        try {
            OrderCreatedEvent orderCreatedEvent = OrderEventMapper.buildOrderCreatedEvent(savedOrder);
            log.info("Calling orderEventService.save() with eventId: {}", orderCreatedEvent.getEventId());
            orderEventService.save(orderCreatedEvent);
            log.info("Successfully called orderEventService.save()");
        } catch (Exception e) {
            log.error(
                    "Failed to create order event for orderNumber={}: {}",
                    savedOrder.getOrderNumber(),
                    e.getMessage(),
                    e);
        }
        return new CreateOrderResponse(savedOrder.getOrderNumber());
    }

    @Override
    public List<OrderSummary> findOrders(String userName) {
        return orderRepository.findByUserName(userName);
    }

    @Override
    public Optional<OrderDTO> findUserOrder(String userName, String orderNumber) {
        return orderRepository
                .findByUserNameAndOrderNumber(userName, orderNumber)
                .map(OrderMapper::convertToDTO);
    }

    @Override
    public void processNewOrders() {
        List<OrderEntity> orders = orderRepository.findOrderByStatus(OrderStatus.NEW);
        log.info("Found {} new orders to process", orders.size());
        for (OrderEntity order : orders) {
            this.process(order);
        }
    }

    private void process(OrderEntity order) {
        try {
            if (canBeDelivered(order)) {
                log.info("OrderNumber: {} can be delivered", order.getOrderNumber());
                orderRepository.updateOrderStatus(order.getOrderNumber(), OrderStatus.DELIVERED);
                try {
                    orderEventService.save(OrderEventMapper.buildOrderDeliveredEvent(order));
                    log.info("Order delivered event saved for orderNumber={}", order.getOrderNumber());
                } catch (Exception e) {
                    log.error("Failed to save order delivered event: {}", e.getMessage(), e);
                }
            } else {
                log.info("OrderNumber: {} can not be delivered", order.getOrderNumber());
                orderRepository.updateOrderStatus(order.getOrderNumber(), OrderStatus.CANCELLED);
                try {
                    orderEventService.save(
                            OrderEventMapper.buildOrderCancelledEvent(order, "Can't deliver to the location"));
                    log.info("Order cancelled event saved for orderNumber={}", order.getOrderNumber());
                } catch (Exception e) {
                    log.error("Failed to save order cancelled event: {}", e.getMessage(), e);
                }
            }
        } catch (RuntimeException e) {
            log.error("Failed to process Order with orderNumber: {}", order.getOrderNumber(), e);
            orderRepository.updateOrderStatus(order.getOrderNumber(), OrderStatus.ERROR);
            try {
                orderEventService.save(OrderEventMapper.buildOrderErrorEvent(order, e.getMessage()));
                log.info("Order error event saved for orderNumber={}", order.getOrderNumber());
            } catch (Exception ex) {
                log.error("Failed to save order error event: {}", ex.getMessage(), ex);
            }
        }
    }

    private boolean canBeDelivered(OrderEntity order) {
        return DELIVERY_ALLOWED_COUNTRIES.contains(
                order.getDeliveryAddress().getCountry().toUpperCase());
    }

    public Optional<OrderDTO> findOrderByOrderId(String orderNumber) {
        return this.orderRepository.findByOrderNumber(orderNumber).map(OrderMapper::convertToDTO);
    }

    public List<OrderEntity> findOrdersByStatus(OrderStatus status) {
        return orderRepository.findOrderByStatus(status);
    }

    public void cancelOrder(String orderNumber) {
        log.info("Cancel order with orderNumber: {}", orderNumber);
        OrderEntity order = this.orderRepository
                .findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderCancellationException(order.getOrderNumber(), "Order is already delivered");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        try {
            orderEventService.save(OrderEventMapper.buildOrderCancelledEvent(order, "Order cancelled by user"));
            log.info("Order cancelled event saved for orderNumber={}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to save order cancelled event: {}", e.getMessage(), e);
        }
    }

    public void updateOrderStatus(String orderNumber, OrderStatus status, String comments) {
        OrderEntity order = orderRepository
                .findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        order.setStatus(status);
        if (comments != null) {
            order.setComments(comments);
        }
        orderRepository.save(order);

        try {
            // Crear eventos para estados específicos que son soportados por OrderEventService
            if (status == OrderStatus.DELIVERED) {
                orderEventService.save(OrderEventMapper.buildOrderDeliveredEvent(order));
                log.info("Order delivered event saved for orderNumber={}", order.getOrderNumber());
            } else if (status == OrderStatus.CANCELLED) {
                orderEventService.save(OrderEventMapper.buildOrderCancelledEvent(order, comments));
                log.info("Order cancelled event saved for orderNumber={}", order.getOrderNumber());
            } else if (status == OrderStatus.ERROR) {
                orderEventService.save(OrderEventMapper.buildOrderErrorEvent(order, comments));
                log.info("Order error event saved for orderNumber={}", order.getOrderNumber());
            }
        } catch (Exception e) {
            log.error("Failed to save order status update event: {}", e.getMessage(), e);
        }
    }
}
