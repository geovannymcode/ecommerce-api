package com.geovannycode.ecommerce.notification.events;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geovannycode.ecommerce.notification.AbstractIT;
import com.geovannycode.ecommerce.notification.domain.model.Address;
import com.geovannycode.ecommerce.notification.domain.model.Customer;
import com.geovannycode.ecommerce.notification.domain.model.OrderCancelledEvent;
import com.geovannycode.ecommerce.notification.infrastructure.adapter.input.messaging.KafkaOrderEventConsumer;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DirectKafkaOrderEventConsumerTest extends AbstractIT {

    @Autowired
    private KafkaOrderEventConsumer kafkaOrderEventConsumer;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer customer;
    private Address address;

    @BeforeEach
    void setUp() {
        customer = new Customer("Geovanny", "geovanny@gmail.com", "999999999");
        address = new Address("addr line 1", null, "Barranquilla", "ATL", "500072", "Colombia");
        Mockito.reset(notificationService);
    }

    @Test
    void shouldHandleOrderCancelledEventDirectly() throws Exception {
        // Arrange
        String orderNumber = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();

        var event = new OrderCancelledEvent(
                eventId, orderNumber, Set.of(), customer, address, "test cancel reason", LocalDateTime.now());

        // Convertir el objeto a JSON
        String eventJson = objectMapper.writeValueAsString(event);
        System.out.println("Evento JSON: " + eventJson);

        // Act - Llamar directamente al método del consumidor
        kafkaOrderEventConsumer.handleOrderCancelledEvent(eventJson);

        // Assert - Verificar que se haya llamado al servicio de notificación
        verify(notificationService).sendOrderCancelledNotification(any());
    }
}
