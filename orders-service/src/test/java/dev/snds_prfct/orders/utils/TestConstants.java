package dev.snds_prfct.orders.utils;

import dev.snds_prfct.orders.constant.OrderStatus;
import dev.snds_prfct.orders.dto.request.OrderCreationRequestDto;
import dev.snds_prfct.orders.dto.response.OrderItemResponseDto;
import dev.snds_prfct.orders.dto.response.OrderResponseDto;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.entity.orders.OrderItem;
import dev.snds_prfct.orders.entity.orders.Product;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestConstants {
    public static final Long TEST_ORDER_ID = 1L;
    public static final Long TEST_CUSTOMER_ID = 1L;
    public static final String TEST_DELIVERY_ADDRESS = "Delivery Address";
    public static final UUID TEST_IDEMPOTENCY_KEY = UUID.randomUUID();

    public static final Long TEST_PRODUCT1_ID = 1L;
    public static final String TEST_PRODUCT1_NAME = "Test Product 1";
    public static final Long TEST_PRODUCT1_PRICE = 100L;

    public static final Long TEST_PRODUCT2_ID = 2L;
    public static final String TEST_PRODUCT2_NAME = "Test Product 2";
    public static final Long TEST_PRODUCT2_PRICE = 100L;

    public static OrderCreationRequestDto getOrderCreationRequestDto() {
        return new OrderCreationRequestDto(
                TEST_IDEMPOTENCY_KEY,
                Map.of(TEST_PRODUCT1_ID, 2, TEST_PRODUCT2_ID, 2),
                TEST_DELIVERY_ADDRESS);
    }

    public static OrderResponseDto getOrderResponseDto() {
        OrderItemResponseDto orderItemResponseDto1 = new OrderItemResponseDto(
                TEST_PRODUCT1_ID,
                TEST_PRODUCT1_ID,
                TEST_PRODUCT1_NAME,
                1,
                TEST_PRODUCT1_PRICE);
        OrderItemResponseDto orderItemResponseDto2 = new OrderItemResponseDto(
                TEST_PRODUCT2_ID,
                TEST_PRODUCT2_ID,
                TEST_PRODUCT2_NAME,
                1,
                TEST_PRODUCT2_PRICE);

        return new OrderResponseDto(
                1L,
                TEST_DELIVERY_ADDRESS,
                Instant.now(),
                OrderStatus.PENDING,
                List.of(orderItemResponseDto1, orderItemResponseDto2),
                TEST_PRODUCT1_PRICE + TEST_PRODUCT2_PRICE);
    }

    public static Order getTestOrder() {
        Product product1 = getProduct1();
        Product product2 = getProduct2();

        OrderItem orderItem1 = OrderItem.builder()
                .id(1L)
                .product(product1)
                .quantity(1)
                .unitPrice(TEST_PRODUCT1_PRICE)
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .id(2L)
                .product(product2)
                .quantity(1)
                .unitPrice(TEST_PRODUCT2_PRICE)
                .build();

        return Order.builder()
                .id(1L)
                .customerId(TEST_CUSTOMER_ID)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .idempotencyKey(TEST_IDEMPOTENCY_KEY)
                .deliveryAddress(TEST_DELIVERY_ADDRESS)
                .orderItems(List.of(orderItem1, orderItem2))
                .build();
    }

    public static Product getProduct2() {
        return Product.builder()
                .id(TEST_PRODUCT2_ID)
                .name(TEST_PRODUCT2_NAME)
                .price(TEST_PRODUCT2_PRICE)
                .isAvailable(true)
                .build();
    }

    public static Product getProduct1() {
        return Product.builder()
                .id(TEST_PRODUCT1_ID)
                .name(TEST_PRODUCT1_NAME)
                .price(TEST_PRODUCT1_PRICE)
                .isAvailable(true)
                .build();
    }
}
