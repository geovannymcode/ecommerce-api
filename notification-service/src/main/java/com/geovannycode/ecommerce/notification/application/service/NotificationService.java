package com.geovannycode.ecommerce.notification.application.service;

import com.geovannycode.ecommerce.notification.domain.model.OrderCancelledEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderCreatedEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderDeliveredEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderErrorEvent;
import com.geovannycode.ecommerce.notification.domain.port.input.NotificationUseCase;
import com.geovannycode.ecommerce.notification.domain.port.output.EmailSenderPort;
import com.geovannycode.ecommerce.notification.infrastructure.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService implements NotificationUseCase {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailSenderPort emailSender;
    private final ApplicationProperties properties;

    public NotificationService(EmailSenderPort emailSender, ApplicationProperties properties) {
        this.emailSender = emailSender;
        this.properties = properties;
    }

    @Override
    public void sendOrderCreatedNotification(OrderCreatedEvent event) {
        String message =
                """
                ===================================================
                Order Created Notification
                ----------------------------------------------------
                Dear %s,
                Your order with orderNumber: %s has been created successfully.

                Thanks,
                BookStore Team
                ===================================================
                """
                        .formatted(event.customer().name(), event.orderNumber());
        log.info("\n{}", message);
        emailSender.sendEmail(event.customer().email(), "Order Created Notification", message);
    }

    @Override
    public void sendOrderDeliveredNotification(OrderDeliveredEvent event) {
        String message =
                """
                ===================================================
                Order Delivered Notification
                ----------------------------------------------------
                Dear %s,
                Your order with orderNumber: %s has been delivered successfully.

                Thanks,
                BookStore Team
                ===================================================
                """
                        .formatted(event.customer().name(), event.orderNumber());
        log.info("\n{}", message);
        emailSender.sendEmail(event.customer().email(), "Order Delivered Notification", message);
    }

    @Override
    public void sendOrderCancelledNotification(OrderCancelledEvent event) {
        String message =
                """
                ===================================================
                Order Cancelled Notification
                ----------------------------------------------------
                Dear %s,
                Your order with orderNumber: %s has been cancelled.
                Reason: %s

                Thanks,
                BookStore Team
                ===================================================
                """
                        .formatted(event.customer().name(), event.orderNumber(), event.reason());
        log.info("\n{}", message);
        emailSender.sendEmail(event.customer().email(), "Order Cancelled Notification", message);
    }

    @Override
    public void sendOrderErrorEventNotification(OrderErrorEvent event) {
        String message =
                """
                ===================================================
                Order Processing Failure Notification
                ----------------------------------------------------
                Hi %s,
                The order processing failed for orderNumber: %s.
                Reason: %s

                Thanks,
                BookStore Team
                ===================================================
                """
                        .formatted(properties.getSupportEmail(), event.orderNumber(), event.reason());
        log.info("\n{}", message);
        emailSender.sendEmail(properties.getSupportEmail(), "Order Processing Failure Notification", message);
    }
}
