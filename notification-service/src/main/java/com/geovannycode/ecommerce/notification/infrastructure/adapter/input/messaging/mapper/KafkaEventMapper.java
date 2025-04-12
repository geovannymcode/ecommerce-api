package com.geovannycode.ecommerce.notification.infrastructure.adapter.input.messaging.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geovannycode.ecommerce.notification.domain.model.Address;
import com.geovannycode.ecommerce.notification.domain.model.Customer;
import com.geovannycode.ecommerce.notification.domain.model.OrderCancelledEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderCreatedEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderDeliveredEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderErrorEvent;
import com.geovannycode.ecommerce.notification.domain.model.OrderItem;
import com.geovannycode.ecommerce.notification.infrastructure.config.ApplicationProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventMapper {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventMapper.class);
    private final ObjectMapper objectMapper;
    private final String supportEmail;

    public KafkaEventMapper(ObjectMapper objectMapper, ApplicationProperties properties) {
        this.objectMapper = objectMapper;
        this.supportEmail = properties.getSupportEmail();
    }

    public OrderCreatedEvent mapToOrderCreatedEvent(String message) throws Exception {

        log.info("Starting to map message of length: {}", message.length());

        // Si el mensaje está entre comillas y contiene caracteres escapados, eliminamos las comillas
        if (message.startsWith("\"") && message.endsWith("\"")) {
            // Eliminar las comillas externas
            message = message.substring(1, message.length() - 1);
            // Desescapar el JSON interno
            message = message.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\/", "/");
            log.info("Unescaped message: {}", message);
        }

        JsonNode root = objectMapper.readTree(message);

        // Extraer y registrar los campos individuales para debug
        String eventId = root.has("eventId") ? root.get("eventId").asText() : "unknown-event-id";
        log.info("Extracted eventId: {}", eventId);

        String orderNumber = root.has("orderNumber") ? root.get("orderNumber").asText() : "unknown";
        log.info("Extracted orderNumber: {}", orderNumber);

        // Verificar si customer existe y registrar su contenido
        if (root.has("customer") && !root.get("customer").isNull()) {
            JsonNode customerNode = root.get("customer");
            log.info("Customer node exists: {}", customerNode);
        } else {
            log.error("Customer node is missing or null in the message");
        }

        // Continuar con el mapeo
        EventData eventData = extractEventData(root);

        return new OrderCreatedEvent(
                eventData.eventId,
                eventData.orderNumber,
                eventData.items,
                eventData.customer,
                eventData.address,
                eventData.createdAt);
    }

    public OrderDeliveredEvent mapToOrderDeliveredEvent(String message) throws Exception {
        JsonNode root = parseMessage(message);
        EventData eventData = extractEventData(root);

        return new OrderDeliveredEvent(
                eventData.eventId,
                eventData.orderNumber,
                eventData.items,
                eventData.customer,
                eventData.address,
                eventData.createdAt);
    }

    public OrderCancelledEvent mapToOrderCancelledEvent(String message) throws Exception {
        JsonNode root = parseMessage(message);
        EventData eventData = extractEventData(root);
        String reason = root.has("reason") ? root.get("reason").asText("No reason provided") : "No reason provided";

        return new OrderCancelledEvent(
                eventData.eventId,
                eventData.orderNumber,
                eventData.items,
                eventData.customer,
                eventData.address,
                reason,
                eventData.createdAt);
    }

    public OrderErrorEvent mapToOrderErrorEvent(String message) throws Exception {
        JsonNode root = parseMessage(message);
        EventData eventData = extractEventData(root);
        String reason = root.has("reason") ? root.get("reason").asText("Unknown error") : "Unknown error";

        return new OrderErrorEvent(
                eventData.eventId,
                eventData.orderNumber,
                eventData.items,
                eventData.customer,
                eventData.address,
                reason,
                eventData.createdAt);
    }

    private JsonNode parseMessage(String message) throws Exception {
        try {
            JsonNode root = objectMapper.readTree(message);
            log.debug("Parsed JSON structure: {}", root.toString());
            return root;
        } catch (Exception e) {
            log.error("Error parsing JSON message: {}", message, e);
            throw e;
        }
    }

    private EventData extractEventData(JsonNode root) {
        EventData data = new EventData();

        // Extraer campos básicos
        data.eventId = root.has("eventId") ? root.get("eventId").asText() : generateRandomId();
        data.orderNumber = root.has("orderNumber") ? root.get("orderNumber").asText() : "unknown";

        // Extraer fecha de creación
        if (root.has("createdAt") && !root.get("createdAt").isNull()) {
            try {
                data.createdAt = LocalDateTime.parse(root.get("createdAt").asText());
            } catch (Exception e) {
                log.warn("Could not parse createdAt date: {}", e.getMessage());
                data.createdAt = LocalDateTime.now();
            }
        } else {
            data.createdAt = LocalDateTime.now();
        }

        // Extraer objetos anidados
        data.customer = root.has("customer") ? mapCustomer(root.get("customer")) : new Customer();
        data.address = root.has("deliveryAddress") ? mapAddress(root.get("deliveryAddress")) : new Address();
        data.items = root.has("items") ? mapItems(root.get("items")) : new HashSet<>();

        return data;
    }

    private String generateRandomId() {
        return UUID.randomUUID().toString();
    }

    private Customer mapCustomer(JsonNode node) {
        Customer customer = new Customer();

        if (node == null || node.isNull()) {
            Customer defaultCustomer = new Customer();
            defaultCustomer.setName("Unknown Customer");
            defaultCustomer.setEmail("me@gmail.com"); // Usar el email de soporte como fallback
            defaultCustomer.setPhone("");
            return defaultCustomer;
        }

        // Mapear propiedades con valores por defecto
        customer.setName(node.has("name") ? node.get("name").asText("Customer") : "Customer");

        // Asegurarnos que el email nunca sea nulo
        String email = null;
        if (node.has("email") && !node.get("email").isNull()) {
            email = node.get("email").asText();
        }

        // Si email sigue siendo nulo o vacío, usar un email de soporte
        if (email == null || email.isEmpty()) {
            log.warn("Customer email is null or empty, using support email");
            email = "supportemail@yourdomain.com"; // Usa una dirección de soporte válida
        }

        customer.setEmail(email);
        customer.setPhone(node.has("phone") ? node.get("phone").asText("") : "");

        return customer;
    }

    private Address mapAddress(JsonNode node) {
        if (node == null || node.isNull()) {
            return new Address();
        }

        String addressLine1 =
                node.has("addressLine1") ? node.get("addressLine1").asText("") : "";
        String addressLine2 =
                node.has("addressLine2") ? node.get("addressLine2").asText("") : "";
        String city = node.has("city") ? node.get("city").asText("") : "";
        String state = node.has("state") ? node.get("state").asText("") : "";
        String zipCode = node.has("zipCode") ? node.get("zipCode").asText("") : "";
        String country = node.has("country") ? node.get("country").asText("") : "";

        Address address = new Address();
        address.setAddressLine1(addressLine1);
        address.setAddressLine2(addressLine2);
        address.setCity(city);
        address.setState(state);
        address.setZipCode(zipCode);
        address.setCountry(country);

        return address;
    }

    private Set<OrderItem> mapItems(JsonNode itemsNode) {
        Set<OrderItem> items = new HashSet<>();

        if (itemsNode == null || !itemsNode.isArray()) {
            return items;
        }

        for (JsonNode itemNode : itemsNode) {
            if (itemNode == null || itemNode.isNull()) {
                continue;
            }

            String code = itemNode.has("code") ? itemNode.get("code").asText("") : "";
            String name = itemNode.has("name") ? itemNode.get("name").asText("Unknown Item") : "Unknown Item";

            BigDecimal price = BigDecimal.ZERO;
            if (itemNode.has("price")) {
                try {
                    price = new BigDecimal(itemNode.get("price").asText("0"));
                } catch (NumberFormatException e) {
                    log.warn("Could not parse price: {}", e.getMessage());
                }
            }

            Integer quantity = 0;
            if (itemNode.has("quantity")) {
                try {
                    quantity = itemNode.get("quantity").asInt(0);
                } catch (Exception e) {
                    log.warn("Could not parse quantity: {}", e.getMessage());
                }
            }

            OrderItem item = new OrderItem();
            item.setCode(code);
            item.setName(name);
            item.setPrice(price);
            item.setQuantity(quantity);

            items.add(item);
        }

        return items;
    }

    // Clase auxiliar para transportar datos comunes entre eventos
    private static class EventData {
        String eventId;
        String orderNumber;
        Set<OrderItem> items;
        Customer customer;
        Address address;
        LocalDateTime createdAt;
    }
}
