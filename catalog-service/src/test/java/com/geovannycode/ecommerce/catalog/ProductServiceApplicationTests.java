package com.geovannycode.ecommerce.catalog;

import com.geovannycode.ecommerce.catalog.infrastructure.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ProductServiceApplicationTests {

    @Test
    void contextLoads() {}
}
