package com.geovannycode.ecommerce.notification.infrastructure.adapter.output.email;

import com.geovannycode.ecommerce.notification.domain.port.output.EmailSenderPort;
import com.geovannycode.ecommerce.notification.infrastructure.config.ApplicationProperties;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JavaMailSenderAdapter implements EmailSenderPort {
    private static final Logger log = LoggerFactory.getLogger(JavaMailSenderAdapter.class);

    private final JavaMailSender emailSender;
    private final String supportEmail;

    public JavaMailSenderAdapter(JavaMailSender emailSender, ApplicationProperties properties) {
        this.emailSender = emailSender;
        this.supportEmail = properties.getSupportEmail();
    }

    @Override
    public void sendEmail(String recipient, String subject, String content) {
        try {
            if (!StringUtils.hasText(recipient)) {
                log.error("Cannot send email: recipient address is null or empty");
                throw new IllegalArgumentException("Recipient email address cannot be null or empty");
            }
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(supportEmail);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(content);
            emailSender.send(mimeMessage);
            log.info("Email sent to: {}", recipient);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", recipient, e.getMessage());
            throw new RuntimeException("Error while sending email", e);
        }
    }
}
