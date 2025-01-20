package com.geovannycode.ecommerce.payment.infrastructure.output.persistence.repository;

import com.geovannycode.ecommerce.payment.application.ports.output.CreditCardRepository;
import com.geovannycode.ecommerce.payment.domain.model.CreditCard;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCreditCardRepository implements CreditCardRepository {
    private final JdbcTemplate jdbcTemplate;

    public JpaCreditCardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<CreditCard> findByCardNumber(String cardNumber) {
        String sql =
                "select id, customer_name, card_number, cvv, expiry_month, expiry_year from credit_cards where card_number = ?";
        var creditCards =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) ->
                                new CreditCard(
                                        rs.getLong("id"),
                                        rs.getString("customer_name"),
                                        rs.getString("card_number"),
                                        rs.getString("cvv"),
                                        rs.getInt("expiry_month"),
                                        rs.getInt("expiry_year")),
                        cardNumber);
        return creditCards.isEmpty() ? Optional.empty() : Optional.of(creditCards.get(0));
    }
}
