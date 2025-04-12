package com.geovannycode.ecommerce.notification.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "notification")
public class ApplicationProperties {
    private String orderCreatedTopic;
    private String orderDeliveredTopic;
    private String orderCancelledTopic;
    private String orderErrorTopic;
    private String supportEmail;

    public String getOrderCreatedTopic() {
        return orderCreatedTopic;
    }

    public void setOrderCreatedTopic(String orderCreatedTopic) {
        this.orderCreatedTopic = orderCreatedTopic;
    }

    public String getOrderDeliveredTopic() {
        return orderDeliveredTopic;
    }

    public void setOrderDeliveredTopic(String orderDeliveredTopic) {
        this.orderDeliveredTopic = orderDeliveredTopic;
    }

    public String getOrderCancelledTopic() {
        return orderCancelledTopic;
    }

    public void setOrderCancelledTopic(String orderCancelledTopic) {
        this.orderCancelledTopic = orderCancelledTopic;
    }

    public String getOrderErrorTopic() {
        return orderErrorTopic;
    }

    public void setOrderErrorTopic(String orderErrorTopic) {
        this.orderErrorTopic = orderErrorTopic;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }
}
