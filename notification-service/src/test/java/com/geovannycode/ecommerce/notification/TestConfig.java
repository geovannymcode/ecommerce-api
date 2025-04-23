package com.geovannycode.ecommerce.notification;

import com.geovannycode.ecommerce.notification.application.service.NotificationService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public NotificationService notificationService() {
        return Mockito.mock(NotificationService.class);
    }
}
