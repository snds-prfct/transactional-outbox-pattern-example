package dev.snds_prfct.orders.mapper;

import dev.snds_prfct.orders.constant.OrderStatus;
import dev.snds_prfct.orders.dto.request.OrderCreationRequestDto;
import dev.snds_prfct.orders.dto.response.OrderItemResponseDto;
import dev.snds_prfct.orders.dto.response.OrderResponseDto;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.entity.orders.OrderItem;
import dev.snds_prfct.orders.entity.orders.Product;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderItems", source = "productsAmountByProductId", qualifiedByName = "mapProductsToOrderItems")
    @Mapping(target = "customerId", expression = "java( dev.snds_prfct.orders.security.PrincipalUtils.getCurrentUserId() )")
    Order map(OrderCreationRequestDto orderCreationRequestDto, @Context List<Product> products);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "orderItems", source = "orderItems", qualifiedByName = "mapOrderItemsToOrderResponseDtoItems")
    @Mapping(target = "totalPrice", source = ".", qualifiedByName = "calculateTotalPrice")
    OrderResponseDto map(Order order);

    @Named("mapProductsToOrderItems")
    default List<OrderItem> mapProductsToOrderItems(Map<Long, Integer> productsAmountByProductId, @Context List<Product> products) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : productsAmountByProductId.entrySet()) {
            Product product = products.stream().filter(p -> entry.getKey().equals(p.getId())).toList().get(0);

            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(entry.getValue());
            orderItem.setProduct(product);
            orderItem.setUnitPrice(product.getPrice());
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    @Named("mapOrderItemsToOrderResponseDtoItems")
    default List<OrderItemResponseDto> mapOrderItemsToOrderResponseDtoItems(List<OrderItem> orderItems) {
        List<OrderItemResponseDto> orderItemResponseDtos = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            orderItemResponseDtos.add(
                    new OrderItemResponseDto(orderItem.getId(), orderItem.getProduct().getId(), orderItem.getProduct().getName(), orderItem.getQuantity(), orderItem.getUnitPrice()));
        }
        return orderItemResponseDtos;
    }

    @Named("calculateTotalPrice")
    default Long calculateTotalPrice(Order order) {
        return order.getOrderItems().stream()
                .mapToLong(orderItem -> orderItem.getQuantity() * orderItem.getUnitPrice())
                .boxed()
                .reduce(0L, Long::sum);
    }

    @AfterMapping
    default void doOrder(@MappingTarget Order order) {
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());
        order.getOrderItems()
                .forEach(orderItem -> orderItem.setOrder(order));
    }
}
