package dev.snds_prfct.orders.kafka.message;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record OrderMessage(
        OrderEventType type,
        Instant timestamp,
        String source,
        Order order) {

    public enum OrderEventType {
        ORDER_CREATED, ORDER_CANCELLED
    }

    @Builder
    public record Order(
            Long orderId,
            Long customerId,
            Instant createdAt,
            String deliveryAddress,
            List<Item> items) {

        @Builder
        public record Item(
                Long id,
                Long productId,
                String productName,
                Integer quantity,
                Long unitPrice) {
        }
    }
}
