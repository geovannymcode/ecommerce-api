package com.geovannycode.ecommerce.order.domain.events;

import com.geovannycode.ecommerce.order.domain.model.Address;
import com.geovannycode.ecommerce.order.domain.model.Customer;
import com.geovannycode.ecommerce.order.infrastructure.input.api.dto.OrderItemDTO;
import java.time.LocalDateTime;
import java.util.Set;

public record OrderDeliveredEvent(
        String eventId,
        String orderNumber,
        Set<OrderItemDTO> items,
        Customer customer,
        Address deliveryAddress,
        LocalDateTime createdAt) {}
