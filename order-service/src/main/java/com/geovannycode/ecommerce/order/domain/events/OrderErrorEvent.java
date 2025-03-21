package com.geovannycode.ecommerce.order.domain.events;

import com.geovannycode.ecommerce.order.infrastructure.input.api.dto.OrderItemDTO;
import java.time.LocalDateTime;
import java.util.Set;

public record OrderErrorEvent(
        String eventId,
        String orderNumber,
        Set<OrderItemDTO> items,
        Customer customer,
        Address deliveryAddress,
        String reason,
        LocalDateTime createdAt) {}
