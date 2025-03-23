package com.geovannycode.ecommerce.order.infrastructure.output.clients.catalog;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component("orderProductServiceClient")
public class ProductServiceClient {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceClient.class);

    private final RestClient restClient;

    ProductServiceClient(@Qualifier("orderRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<Product> getProductByCode(String code) {
        log.info("Fetching product for code: {}", code);
        var product =
                restClient.get().uri("/api/products/{code}", code).retrieve().body(Product.class);
        return Optional.ofNullable(product);
    }
}
