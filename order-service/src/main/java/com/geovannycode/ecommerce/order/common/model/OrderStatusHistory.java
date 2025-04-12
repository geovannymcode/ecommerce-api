package com.geovannycode.ecommerce.order.common.model;

import com.geovannycode.ecommerce.order.common.model.enums.OrderStatus;
import java.time.LocalDateTime;

public class OrderStatusHistory {
    private final String orderNumber;
    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;
    private final String comments;
    private final LocalDateTime changedAt;
    private final String changedBy;

    public OrderStatusHistory(
            String orderNumber,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String comments,
            LocalDateTime changedAt,
            String changedBy) {
        this.orderNumber = orderNumber;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.comments = comments;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public OrderStatus getPreviousStatus() {
        return previousStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public String getComments() {
        return comments;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }
}
