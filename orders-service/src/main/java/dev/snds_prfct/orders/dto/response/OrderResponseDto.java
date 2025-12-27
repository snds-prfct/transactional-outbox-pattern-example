package dev.snds_prfct.orders.dto.response;

import dev.snds_prfct.orders.constant.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderResponseDto(
        Long orderId,
        String deliveryAddress,
        Instant createdAt,
        OrderStatus status,
        List<OrderItemResponseDto> orderItems,
        Long totalPrice) {
}
