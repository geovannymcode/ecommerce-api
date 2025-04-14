package com.geovannycode.ecommerce.notification.application.service;

import com.geovannycode.ecommerce.notification.domain.model.Customer;
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
        if (!validateCustomerAndEmail(event.getCustomer(), event.getOrderNumber())) {
            return;
        }

        sendNotificationEmail(
                event.getCustomer().getEmail(),
                "Order Created Notification",
                createNotificationMessage(
                        "Order Created Notification", event.getCustomer().getName(), event.getOrderNumber(), null));
    }

    @Override
    public void sendOrderDeliveredNotification(OrderDeliveredEvent event) {
        if (!validateCustomerAndEmail(event.getCustomer(), event.getOrderNumber())) {
            return;
        }

        sendNotificationEmail(
                event.getCustomer().getEmail(),
                "Order Delivered Notification",
                createNotificationMessage(
                        "Order Delivered Notification", event.getCustomer().getName(), event.getOrderNumber(), null));
    }

    @Override
    public void sendOrderCancelledNotification(OrderCancelledEvent event) {
        if (!validateCustomerAndEmail(event.getCustomer(), event.getOrderNumber())) {
            return;
        }

        sendNotificationEmail(
                event.getCustomer().getEmail(),
                "Order Cancelled Notification",
                createNotificationMessage(
                        "Order Cancelled Notification",
                        event.getCustomer().getName(),
                        event.getOrderNumber(),
                        "Reason: " + event.getReason()));
    }

    @Override
    public void sendOrderErrorEventNotification(OrderErrorEvent event) {
        if (!validateCustomerAndEmail(event.getCustomer(), event.getOrderNumber())) {
            return;
        }

        sendNotificationEmail(
                event.getCustomer().getEmail(),
                "Order Processing Failure Notification",
                createNotificationMessage(
                        "Order Processing Failure Notification",
                        event.getCustomer().getName(),
                        event.getOrderNumber(),
                        "Reason: " + event.getReason()));
    }

    private boolean validateCustomerAndEmail(Customer customer, String orderNumber) {
        if (customer == null) {
            log.error("Cannot send notification: Customer is null for orderNumber: {}", orderNumber);
            return false;
        }

        if (customer.getEmail() == null || customer.getEmail().isEmpty()) {
            log.error("Cannot send notification: Customer email is null or empty for orderNumber: {}", orderNumber);
            return false;
        }

        return true;
    }

    private String createNotificationMessage(
            String notificationType, String customerName, String orderNumber, String additionalInfo) {
        String baseMessage =
                """
            ===================================================
            %s
            ----------------------------------------------------
            Dear %s,
            Your order with orderNumber: %s has been %s.
            %s

            Thanks,
            BookStore Team
            ===================================================
            """;

        String actionDescription = getActionDescription(notificationType);

        return baseMessage.formatted(
                notificationType,
                customerName,
                orderNumber,
                actionDescription,
                additionalInfo != null ? additionalInfo : "");
    }

    private static String getActionDescription(String notificationType) {
        String actionDescription;
        switch (notificationType) {
            case "Order Created Notification" -> actionDescription = "created successfully";
            case "Order Delivered Notification" -> actionDescription = "delivered successfully";
            case "Order Cancelled Notification" -> actionDescription = "cancelled";
            case "Order Processing Failure Notification" -> actionDescription = "processing failed";
            default -> actionDescription = "processed";
        }
        return actionDescription;
    }

    private void sendNotificationEmail(String email, String subject, String message) {
        log.info("\n{}", message);
        emailSender.sendEmail(email, subject, message);
    }
}
