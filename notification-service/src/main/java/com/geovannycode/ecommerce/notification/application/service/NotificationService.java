package com.geovannycode.ecommerce.notification.application.service;

import com.geovannycode.ecommerce.notification.domain.model.Customer;
import com.geovannycode.ecommerce.notification.domain.model.OrderCancelledEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderCreatedEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderDeliveredEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderErrorEvent;
import com.geovannycode.ecommerce.notification.domain.port.input.NotificationUseCase;
import com.geovannycode.ecommerce.notification.domain.port.output.EmailSenderPort;
import com.geovannycode.ecommerce.notification.infrastructure.config.ApplicationProperties;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService implements NotificationUseCase {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final Duration NOTIFICATION_THROTTLE = Duration.ofMinutes(5);

    // Mapa para almacenar la última notificación enviada a cada orden
    private final Map<String, NotificationRecord> lastNotifications = new ConcurrentHashMap<>();

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

        // Si recientemente se envió una notificación para esta orden, evaluar si enviamos esta
        if (!shouldSendNotification(event.getOrderNumber(), "Order Created")) {
            log.info(
                    "Skipping Order Created notification for orderNumber: {} as another notification was sent recently",
                    event.getOrderNumber());
            return;
        }

        sendNotificationEmail(
                event.getCustomer().getEmail(),
                "Order Created Notification",
                createNotificationMessage(
                        "Order Created Notification", event.getCustomer().getName(), event.getOrderNumber(), null));

        // Registrar que se envió una notificación
        recordNotification(event.getOrderNumber(), "Order Created");
    }

    @Override
    public void sendOrderDeliveredNotification(OrderDeliveredEvent event) {
        if (!validateCustomerAndEmail(event.getCustomer(), event.getOrderNumber())) {
            return;
        }

        NotificationRecord lastNotification = lastNotifications.get(event.getOrderNumber());
        if (lastNotification == null
                && event.getCreatedAt() != null
                && Duration.between(event.getCreatedAt(), LocalDateTime.now()).compareTo(Duration.ofMinutes(1)) < 0) {

            log.info("Order was created and delivered very quickly: {}", event.getOrderNumber());
        }

        // Solo enviamos notificación normal de entrega
        sendNotificationEmail(
                event.getCustomer().getEmail(),
                "Order Delivered Notification",
                createNotificationMessage(
                        "Order Delivered Notification", event.getCustomer().getName(), event.getOrderNumber(), null));

        recordNotification(event.getOrderNumber(), "Order Delivered");
    }

    @Override
    public void sendOrderCancelledNotification(OrderCancelledEvent event) {
        if (!validateCustomerAndEmail(event.getCustomer(), event.getOrderNumber())) {
            return;
        }

        // Las notificaciones de cancelación son importantes y siempre se envían
        sendNotificationEmail(
                event.getCustomer().getEmail(),
                "Order Cancelled Notification",
                createNotificationMessage(
                        "Order Cancelled Notification",
                        event.getCustomer().getName(),
                        event.getOrderNumber(),
                        "Reason: " + event.getReason()));

        recordNotification(event.getOrderNumber(), "Order Cancelled");
    }

    @Override
    public void sendOrderErrorEventNotification(OrderErrorEvent event) {
        if (!validateCustomerAndEmail(event.getCustomer(), event.getOrderNumber())) {
            return;
        }

        // Las notificaciones de error son importantes y siempre se envían
        sendNotificationEmail(
                event.getCustomer().getEmail(),
                "Order Processing Failure Notification",
                createNotificationMessage(
                        "Order Processing Failure Notification",
                        event.getCustomer().getName(),
                        event.getOrderNumber(),
                        "Reason: " + event.getReason()));

        recordNotification(event.getOrderNumber(), "Order Error");
    }

    private boolean shouldSendNotification(String orderNumber, String notificationType) {
        NotificationRecord lastNotification = lastNotifications.get(orderNumber);

        // Si no hay notificación previa, siempre enviar
        if (lastNotification == null) {
            return true;
        }

        // Si han pasado más de 5 minutos desde la última notificación, enviar
        if (Duration.between(lastNotification.timestamp, LocalDateTime.now()).compareTo(NOTIFICATION_THROTTLE) > 0) {
            return true;
        }

        // Las notificaciones importantes (error, cancelación) siempre se envían
        if (notificationType.contains("Error") || notificationType.contains("Cancelled")) {
            return true;
        }

        // Para notificaciones de creación, no enviar si ya hubo una notificación reciente
        // (probablemente pronto habrá una notificación de entrega)
        return false;
    }

    private void recordNotification(String orderNumber, String notificationType) {
        lastNotifications.put(orderNumber, new NotificationRecord(notificationType, LocalDateTime.now()));

        // Limpieza de registros antiguos (simplificada, en un entorno real usaríamos un job)
        if (lastNotifications.size() > 1000) {
            LocalDateTime cutoff = LocalDateTime.now().minus(Duration.ofHours(24));
            lastNotifications
                    .entrySet()
                    .removeIf(entry -> entry.getValue().timestamp.isBefore(cutoff));
        }
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

    private static class NotificationRecord {
        final String type;
        final LocalDateTime timestamp;

        NotificationRecord(String type, LocalDateTime timestamp) {
            this.type = type;
            this.timestamp = timestamp;
        }
    }
}
