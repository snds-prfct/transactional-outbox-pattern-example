package dev.snds_prfct.orders.mapper;

import dev.snds_prfct.orders.dto.request.OrderCreationRequestDto;
import dev.snds_prfct.orders.dto.response.OrderResponseDto;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.entity.orders.OrderItem;
import dev.snds_prfct.orders.entity.orders.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;

import static dev.snds_prfct.orders.utils.TestConstants.TEST_PRODUCT1_NAME;
import static dev.snds_prfct.orders.utils.TestConstants.TEST_PRODUCT1_PRICE;
import static dev.snds_prfct.orders.utils.TestConstants.TEST_PRODUCT2_NAME;
import static dev.snds_prfct.orders.utils.TestConstants.TEST_PRODUCT2_PRICE;
import static dev.snds_prfct.orders.utils.TestConstants.getOrderCreationRequestDto;
import static dev.snds_prfct.orders.utils.TestConstants.getOrderResponseDto;
import static dev.snds_prfct.orders.utils.TestConstants.getProduct1;
import static dev.snds_prfct.orders.utils.TestConstants.getProduct2;
import static dev.snds_prfct.orders.utils.TestConstants.getTestOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class OrderMapperTest {

    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    @Test
    void testOrderEntityToOrderResponseDtoMapping() {
        // given
        Order order = getTestOrder();
        OrderResponseDto orderResponseDto = getOrderResponseDto();


        // when
        OrderResponseDto map = orderMapper.map(order);

        // then
        assertThat(map)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(orderResponseDto);
    }

    @Test
    void testOrderCreationRequestDtoToOrderEntityMapping() {
        // given
        OrderCreationRequestDto orderCreationRequestDto = getOrderCreationRequestDto();
        Order expectedOrder = getTestOrder();
        expectedOrder.setId(null);
        List<Product> products = List.of(getProduct1(), getProduct2());

        // when
        Order order = orderMapper.map(orderCreationRequestDto, products);

        // then
        assertThat(order)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class, OrderItem.class)
                .isEqualTo(expectedOrder);

        assertThat(order.getOrderItems())
                .extracting("product.name", "quantity", "unitPrice")
                .containsExactlyInAnyOrder(
                        tuple(TEST_PRODUCT1_NAME, 2, TEST_PRODUCT1_PRICE),
                        tuple(TEST_PRODUCT2_NAME, 2, TEST_PRODUCT2_PRICE));
    }
}