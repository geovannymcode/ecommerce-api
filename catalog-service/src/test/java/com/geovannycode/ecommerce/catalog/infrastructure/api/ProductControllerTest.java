package com.geovannycode.ecommerce.catalog.infrastructure.api;

import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geovannycode.ecommerce.catalog.TestcontainersConfiguration;
import com.geovannycode.ecommerce.catalog.infrastructure.api.dto.ProductDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Sql("/test-data.sql")
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.totalElements", is(15)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(false)))
                .andExpect(jsonPath("$.hasNext", is(true)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldGetProductByCode() throws Exception {
        mockMvc.perform(get("/api/products/{code}", "P100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("P100")))
                .andExpect(jsonPath("$.name", is("The Hunger Games")));
    }

    @Test
    void shouldReturnNotFoundWhenProductCodeNotExists() throws Exception {
        String code = "invalid_product_code";
        mockMvc.perform(get("/api/products/{code}", code))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("Product Not Found")))
                .andExpect(jsonPath("$.detail", is("Product with code " + code + " not found")));
    }

    @Test
    void shouldSearchProducts() throws Exception {
        mockMvc.perform(get("/api/products/search").param("query", "The").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value(is("The Alchemist")));
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductDto productDto = new ProductDto(
                "P115",
                "New Product",
                "This is a new product.",
                "https://example.com/new-product.jpg",
                new BigDecimal("25.0"),
                100,
                null,
                BigDecimal.ZERO);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is("P115")))
                .andExpect(jsonPath("$.name", is("New Product")));
    }

    @Test
    void shouldDeleteProduct() throws Exception {

        mockMvc.perform(get("/api/products/{code}", "P100")).andExpect(status().isOk());

        mockMvc.perform(delete("/api/products/{code}", "P100")).andExpect(status().isOk());

        mockMvc.perform(get("/api/products/{code}", "P100")).andExpect(status().isNotFound());
    }
}
