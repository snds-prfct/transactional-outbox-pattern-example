package dev.snds_prfct.orders.mapper;

import dev.snds_prfct.orders.constant.OrderStatus;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.message.OrderMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderMessageMapper {

    @Value("${spring.application.name}")
    private String appName;

    public OrderMessage map(Order order) {
        return OrderMessage.builder()
                .type(resolveEventType(order.getStatus()))
                .timestamp(Instant.now())
                .source(appName)
                .order(OrderMessage.Order.builder()
                        .orderId(order.getId())
                        .customerId(order.getCustomerId())
                        .createdAt(order.getCreatedAt())
                        .deliveryAddress(order.getDeliveryAddress())
                        .items(order.getOrderItems().stream()
                                .map(orderItem -> OrderMessage.Order.Item.builder()
                                        .id(orderItem.getId())
                                        .productId(orderItem.getProduct().getId())
                                        .productName(orderItem.getProduct().getName())
                                        .quantity(orderItem.getQuantity())
                                        .unitPrice(orderItem.getUnitPrice())
                                        .build())
                                .toList())
                        .build())
                .build();
    }

    private OrderMessage.OrderEventType resolveEventType(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case PENDING -> OrderMessage.OrderEventType.ORDER_CREATED;
            case CANCELED -> OrderMessage.OrderEventType.ORDER_CANCELLED;
            default -> throw new RuntimeException("Unsupported order status: %s".formatted(orderStatus));
        };
    }
}
