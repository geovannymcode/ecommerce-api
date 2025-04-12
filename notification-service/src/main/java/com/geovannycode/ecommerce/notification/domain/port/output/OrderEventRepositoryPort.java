package com.geovannycode.ecommerce.notification.domain.port.output;

public interface OrderEventRepositoryPort {
    boolean existsByEventId(String eventId);
    // void save(OrderEvent orderEvent); // Usa la entidad de dominio, no la de JPA
}
