package com.geovannycode.bookstore.webapp.config;

import com.geovannycode.bookstore.webapp.infrastructure.clients.catalog.CatalogServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientsConfig {
    private final ApplicationProperties properties;

    ClientsConfig(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Bean
    CatalogServiceClient catalogServiceClient() {
        RestClient restClient = RestClient.create(properties.apiGatewayUrl());
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(CatalogServiceClient.class);
    }
    /*
       @Bean
       OrderServiceClient orderServiceClient() {
           RestClient restClient = RestClient.create(properties.apiGatewayUrl());
           HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                   .build();
           return factory.createClient(OrderServiceClient.class);
       }
    */
}
