package com.geovannycode.ecommerce.notification.domain.port.output;

public interface OrderEventRepositoryPort {
    boolean existsByEventId(String eventId);

    void save(String eventId);
}
