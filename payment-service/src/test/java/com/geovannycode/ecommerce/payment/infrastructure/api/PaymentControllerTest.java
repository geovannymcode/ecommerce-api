package com.geovannycode.ecommerce.payment.infrastructure.api;

import com.geovannycode.ecommerce.payment.domain.model.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.geovannycode.ecommerce.payment.TestcontainersConfiguration;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Sql("/test-data.sql")
public class PaymentControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAcceptPaymentSuccessfully() throws Exception {
        String requestBody = """
                {
                    "cardNumber": "1111222233334444",
                    "cvv": "123",
                    "expiryMonth": 2,
                    "expiryYear": 2030
                }
                """;

        mockMvc.perform(post("/api/payments/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", is(PaymentStatus.ACCEPTED.name())));
    }

    @Test
    void shouldRejectPaymentWhenCVVIsIncorrect() throws Exception {
        String requestBody = """
                {
                    "cardNumber": "1111222233334444",
                    "cvv": "111",
                    "expiryMonth": 2,
                    "expiryYear": 2030
                }
                """;

        mockMvc.perform(post("/api/payments/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", is(PaymentStatus.REJECTED.name())));
    }

    @Test
    void shouldFailWhenMandatoryDataIsMissing() throws Exception {
        String requestBody = """
                {
                    "cardNumber": "1111222233334444",
                    "cvv": "111"
                }
                """;

        mockMvc.perform(post("/api/payments/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
