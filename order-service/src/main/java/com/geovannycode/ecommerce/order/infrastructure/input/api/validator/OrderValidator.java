package com.geovannycode.ecommerce.order.infrastructure.input.api.validator;

import com.geovannycode.ecommerce.order.domain.exception.InvalidOrderException;
import com.geovannycode.ecommerce.order.infrastructure.input.api.dto.CreateOrderRequest;
import com.geovannycode.ecommerce.order.infrastructure.input.api.dto.OrderItemDTO;
import com.geovannycode.ecommerce.order.infrastructure.output.clients.catalog.Product;
import com.geovannycode.ecommerce.order.infrastructure.output.clients.catalog.ProductServiceClient;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderValidator {
    private static final Logger log = LoggerFactory.getLogger(OrderValidator.class);

    private final ProductServiceClient client;

    OrderValidator(ProductServiceClient client) {
        this.client = client;
    }

    public void validate(CreateOrderRequest request) {
        Set<OrderItemDTO> items = request.items();
        for (OrderItemDTO item : items) {
            Product product = client.getProductByCode(item.code())
                    .orElseThrow(() -> new InvalidOrderException("Invalid Product code:" + item.code()));
            if (item.price().compareTo(product.price()) != 0) {
                log.error(
                        "Product price not matching. Actual price:{}, received price:{}",
                        product.price(),
                        item.price());
                throw new InvalidOrderException("Product price not matching");
            }
        }
    }
}
