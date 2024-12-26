package com.geovannycode.ecommerce.catalog;

import com.geovannycode.ecommerce.catalog.infrastructure.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestCatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(CatalogServiceApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
