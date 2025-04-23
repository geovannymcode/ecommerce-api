package com.geovannycode.ecommerce.notification;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.geovannycode.ecommerce.notification.application.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Import({ContainersConfig.class, TestConfig.class})
public abstract class AbstractIT {

    @Autowired
    protected NotificationService notificationService;
}
