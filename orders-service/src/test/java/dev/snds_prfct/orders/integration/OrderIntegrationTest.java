package dev.snds_prfct.orders.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.snds_prfct.orders.constant.OrderStatus;
import dev.snds_prfct.orders.dto.request.OrderCreationRequestDto;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.entity.orders.OrderItem;
import dev.snds_prfct.orders.test_component.DaoUtils;
import dev.snds_prfct.orders.test_component.TestcontainersConfiguration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DaoUtils daoUtils;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Sql(value = "/sql/init-products.sql")
    @SneakyThrows
    void testNewOrderCreation() {
        // given
        UUID idempotencyKey = UUID.randomUUID();
        String testDeliveryAddress = "Test Address";
        OrderCreationRequestDto orderCreationRequestDto = new OrderCreationRequestDto(
                idempotencyKey,
                Map.of(1L, 2, 2L, 2), testDeliveryAddress);
        String dto = objectMapper.writeValueAsString(orderCreationRequestDto);
        Order expectedOrder = Order.builder()
                .id(1L)
                .customerId(1L)
                .idempotencyKey(idempotencyKey)
                .deliveryAddress(testDeliveryAddress)
                .status(OrderStatus.PENDING)
                .build();

        // when, then
        mockMvc.perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(dto)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(content().json("{\"orderId\": 1}"))
                .andDo(print());
        List<Order> customerOrders = daoUtils.findAllOrdersByCustomerId(1L);

        assertThat(customerOrders).isNotEmpty().hasSize(1);
        assertThat(customerOrders.get(0))
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "orderItems")
                .isEqualTo(expectedOrder);

        List<OrderItem> savedOrderItems = daoUtils.findAllOrderItemsByOrderId(1L);
        assertThat(savedOrderItems).isNotEmpty().hasSize(2);
        assertThat(savedOrderItems)
                .extracting(item -> item.getProduct().getId())
                .containsExactlyInAnyOrder(1L, 2L);
        for (OrderItem orderItem : savedOrderItems) {
            if (orderItem.getProduct().getId() == 1L) {
                assertThat(orderItem.getQuantity()).isEqualTo(2);
                assertThat(orderItem.getUnitPrice()).isEqualTo(10);
            }
            if (orderItem.getProduct().getId() == 2L) {
                assertThat(orderItem.getQuantity()).isEqualTo(2);
                assertThat(orderItem.getUnitPrice()).isEqualTo(25);
            }
        }
    }

    @Test
    @SneakyThrows
    void testGetOrdersWhenCustomerDoesNotHaveAnyOrders() {
        mockMvc.perform(
                        get("/orders")
                                .queryParam("customerId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(content().json("[]"))
                .andDo(print());
    }
}
