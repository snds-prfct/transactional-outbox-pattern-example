package dev.snds_prfct.orders.integration;

import dev.snds_prfct.orders.constant.OrderOutboxEventStatus;
import dev.snds_prfct.orders.constant.OrderOutboxEventType;
import dev.snds_prfct.orders.constant.OrderStatus;
import dev.snds_prfct.orders.dto.request.OrderCreationRequestBody;
import dev.snds_prfct.orders.dto.response.OrderResponseDto;
import dev.snds_prfct.orders.dto.response.PageableResponse;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.entity.orders.OrderItem;
import dev.snds_prfct.orders.entity.outbox.OrderOutboxEvent;
import dev.snds_prfct.orders.test_component.DaoUtils;
import dev.snds_prfct.orders.test_component.TestcontainersConfiguration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Sql(
        scripts = "/sql/truncate-tables-except-products.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@Sql(
        scripts = "/sql/init-products.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DaoUtils daoUtils;
    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @SneakyThrows
    void testNewOrderCreation() {
        // given
        UUID idempotencyKey = UUID.randomUUID();
        String testDeliveryAddress = "Test Address";
        OrderCreationRequestBody orderCreationRequestBody = new OrderCreationRequestBody(
                idempotencyKey,
                Map.of(1L, 2, 2L, 2), testDeliveryAddress);
        String dto = jsonMapper.writeValueAsString(orderCreationRequestBody);
        Order expectedOrder = Order.builder()
                .id(1L)
                .customerId(1L)
                .idempotencyKey(idempotencyKey)
                .deliveryAddress(testDeliveryAddress)
                .status(OrderStatus.PENDING)
                .build();

        // when
        mockMvc.perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(dto)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(content().json("{\"orderId\": 1}"))
                .andDo(print());

        // then
        // checking orders in db
        List<Order> customerOrders = daoUtils.findAllOrdersByCustomerId(1L);

        assertThat(customerOrders)
                .hasSize(1);
        assertThat(customerOrders.get(0))
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "orderItems")
                .isEqualTo(expectedOrder);

        // checking order items in db
        List<OrderItem> savedOrderItems = daoUtils.findAllOrderItemsByOrderId(1L);
        assertThat(savedOrderItems)
                .hasSize(2);
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

        // checking order outbox events in db
        List<OrderOutboxEvent> orderOutboxEvents = daoUtils.findAllOrderOutboxEvents();
        assertThat(orderOutboxEvents)
                .hasSize(1)
                .extracting(OrderOutboxEvent::getType, OrderOutboxEvent::getStatus, OrderOutboxEvent::getCreatedAt)
                .allSatisfy(tuple -> {
                    List<Object> list = tuple.toList();
                    assertThat(list.get(0)).isEqualTo(OrderOutboxEventType.ORDER_CREATED);
                    assertThat(list.get(1)).isEqualTo(OrderOutboxEventStatus.PENDING);
                    assertThat(list.get(2)).isNotNull();
                });
    }

    @Test
    @SneakyThrows
    @Sql(scripts = "/sql/init-order.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    void testOrderCancellation() {
        // when
        mockMvc.perform(
                        patch("/orders/1/cancel")
                )
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(content().json("{\"orderId\": 1}"))
                .andDo(print());

        // then
        List<Order> customerOrders = daoUtils.findAllOrdersByCustomerId(1L);

        assertThat(customerOrders)
                .hasSize(1)
                .extracting(Order::getId, Order::getStatus)
                .containsExactly(tuple(1L, OrderStatus.CANCELED));

        List<OrderOutboxEvent> orderOutboxEvents = daoUtils.findAllOrderOutboxEvents();

        assertThat(orderOutboxEvents)
                .hasSize(1)
                .extracting(OrderOutboxEvent::getType, OrderOutboxEvent::getStatus)
                .containsExactly(tuple(OrderOutboxEventType.ORDER_CANCELLED, OrderOutboxEventStatus.PENDING));
    }

    @Test
    @SneakyThrows
    void testGetOrdersWhenCustomerDoesNotHaveAnyOrders() {
        MvcResult mvcResult = mockMvc.perform(
                        get("/orders")
                                .queryParam("customerId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andDo(print())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        PageableResponse<OrderResponseDto> pageableResponse = jsonMapper.readValue(contentAsString, new TypeReference<PageableResponse<OrderResponseDto>>() {
        });

        assertThat(pageableResponse.content())
                .isEmpty();
        assertThat(pageableResponse.pagination().page())
                .isEqualTo(0);
        assertThat(pageableResponse.pagination().pages())
                .isEqualTo(0);
        assertThat(pageableResponse.pagination().isLast())
                .isTrue();
    }
}
