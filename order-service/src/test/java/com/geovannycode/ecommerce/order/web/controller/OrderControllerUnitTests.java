package com.geovannycode.ecommerce.order.web.controller;

import static com.geovannycode.ecommerce.order.testdata.TestDataFactory.createOrderRequestWithInvalidCustomer;
import static com.geovannycode.ecommerce.order.testdata.TestDataFactory.createOrderRequestWithInvalidDeliveryAddress;
import static com.geovannycode.ecommerce.order.testdata.TestDataFactory.createOrderRequestWithNoItems;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geovannycode.ecommerce.order.application.service.OrderService;
import com.geovannycode.ecommerce.order.common.model.CreateOrderRequest;
import com.geovannycode.ecommerce.order.infrastructure.input.api.controller.OrderController;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class OrderControllerUnitTests {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @ParameterizedTest(name = "[{index}]-{0}")
    @MethodSource("createOrderRequestProvider")
    void shouldReturnBadRequestWhenOrderPayloadIsInvalid(CreateOrderRequest request) throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    static Stream<Arguments> createOrderRequestProvider() {
        return Stream.of(
                arguments(named("Order with Invalid Customer", createOrderRequestWithInvalidCustomer())),
                arguments(named("Order with Invalid Delivery Address", createOrderRequestWithInvalidDeliveryAddress())),
                arguments(named("Order with No Items", createOrderRequestWithNoItems())));
    }
}
