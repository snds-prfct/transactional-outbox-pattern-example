package dev.snds_prfct.orders.web;

import dev.snds_prfct.orders.controller.OrderController;
import dev.snds_prfct.orders.dto.request.OrderCreationRequestBody;
import dev.snds_prfct.orders.dto.response.ErrorDetailsResponseBody;
import dev.snds_prfct.orders.exception.OrderWithSuchIdempotencyKeyAlreadyExistsException;
import dev.snds_prfct.orders.exception.ProductsNotAvailableException;
import dev.snds_prfct.orders.exception.ProductsNotFoundException;
import dev.snds_prfct.orders.service.OrderService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = OrderController.class)
public class OrderControllerTest {

    private static final UUID TEST_IDEMPOTENCY_KEY = UUID.randomUUID();

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @SneakyThrows
    void testOrderCreationWhenProductsNotFound() {
        // given
        Mockito.when(orderService.createOrder(any()))
                .thenThrow(new ProductsNotFoundException(List.of(1L)));
        String orderCreationRequestDtoJson = getOrderCreationRequestDtoJson();
        ErrorDetailsResponseBody expectedErrorResponse = new ErrorDetailsResponseBody(null, HttpStatus.CONFLICT.value(), "Products not found");

        // when then
        String responseJson = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderCreationRequestDtoJson))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();

        ErrorDetailsResponseBody errorDetailsResponseBody = jsonMapper.readValue(responseJson, ErrorDetailsResponseBody.class);
        assertThat(errorDetailsResponseBody)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(expectedErrorResponse);
    }

    @Test
    @SneakyThrows
    void testOrderCreationWhenProductsNotAvailable() {
        // given
        Mockito.when(orderService.createOrder(any()))
                .thenThrow(new ProductsNotAvailableException(List.of(1L)));
        String orderCreationRequestDtoJson = getOrderCreationRequestDtoJson();
        ErrorDetailsResponseBody expectedErrorResponse = new ErrorDetailsResponseBody(
                null,
                HttpStatus.CONFLICT.value(),
                "Products not available");

        // when then
        String responseJson = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderCreationRequestDtoJson))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();

        ErrorDetailsResponseBody errorDetailsResponseBody = jsonMapper.readValue(responseJson, ErrorDetailsResponseBody.class);
        assertThat(errorDetailsResponseBody)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(expectedErrorResponse);
    }

    @Test
    @SneakyThrows
    void testOrderCreationWhenProvidedIdempotencyKeyAlreadyUsed() {
        // given
        Mockito.when(orderService.createOrder(any()))
                .thenThrow(new OrderWithSuchIdempotencyKeyAlreadyExistsException(TEST_IDEMPOTENCY_KEY));
        String orderCreationRequestDtoJson = getOrderCreationRequestDtoJson();
        ErrorDetailsResponseBody expectedErrorResponse = new ErrorDetailsResponseBody(
                null,
                HttpStatus.BAD_REQUEST.value(),
                "The order has already been created");

        // when then
        String responseJson = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderCreationRequestDtoJson))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorDetailsResponseBody errorDetailsResponseBody = jsonMapper.readValue(responseJson, ErrorDetailsResponseBody.class);
        assertThat(errorDetailsResponseBody)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(expectedErrorResponse);
    }

    private String getOrderCreationRequestDtoJson() {
        OrderCreationRequestBody orderCreationRequestBody = new OrderCreationRequestBody(
                TEST_IDEMPOTENCY_KEY,
                Map.of(1L, 2),
                "Delivery Address");
        return jsonMapper.writeValueAsString(orderCreationRequestBody);
    }
}
