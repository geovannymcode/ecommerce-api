package com.geovannycode.ecommerce.order.application.ports.input;

import com.geovannycode.ecommerce.order.infrastructure.input.api.dto.OrderDTO;
import java.util.Optional;

public interface FindUserOrderUseCase {
    Optional<OrderDTO> findUserOrder(String userName, String orderNumber);
}
