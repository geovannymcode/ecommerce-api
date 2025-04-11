package com.geovannycode.ecommerce.notification.domain.port.output;

public interface EmailSenderPort {
    void sendEmail(String recipient, String subject, String content);
}
