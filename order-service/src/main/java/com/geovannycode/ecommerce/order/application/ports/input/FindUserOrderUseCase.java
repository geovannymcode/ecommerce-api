package com.geovannycode.ecommerce.order.application.ports.input;

import com.geovannycode.ecommerce.order.common.model.OrderDTO;
import java.util.Optional;

public interface FindUserOrderUseCase {
    Optional<OrderDTO> findUserOrder(String userName, String orderNumber);
}
