package dev.snds_prfct.orders.service;

import dev.snds_prfct.orders.constant.OrderStatus;
import dev.snds_prfct.orders.dto.request.OrderCreationRequestDto;
import dev.snds_prfct.orders.dto.response.OrderCancelledResponseDto;
import dev.snds_prfct.orders.dto.response.OrderCreatedResponseDto;
import dev.snds_prfct.orders.dto.response.OrderResponseDto;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.entity.orders.Product;
import dev.snds_prfct.orders.exception.CustomerDoesNotHaveOrderWithSuchId;
import dev.snds_prfct.orders.exception.OrderCannotBeCancelledException;
import dev.snds_prfct.orders.exception.OrderNotFoundException;
import dev.snds_prfct.orders.exception.OrderWithSuchIdempotencyKeyAlreadyExistsException;
import dev.snds_prfct.orders.mapper.OrderMapper;
import dev.snds_prfct.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static dev.snds_prfct.orders.security.PrincipalUtils.getCurrentUserId;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderProcessingService orderProcessingService;

    public OrderCreatedResponseDto createOrder(OrderCreationRequestDto orderCreationRequestDto) {
        List<Product> products = validateOrderCreationRequestDto(orderCreationRequestDto);
        Order order = orderMapper.map(orderCreationRequestDto, products);
        Long createdOrderId = orderProcessingService.processOrderCreation(order);
        return new OrderCreatedResponseDto(createdOrderId);
    }

    public List<OrderResponseDto> findOrders() {
        return findAllOrdersByCustomerId().stream()
                .map(orderMapper::map)
                .toList();
    }

    public OrderResponseDto findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    if (!Objects.equals(order.getCustomerId(), getCurrentUserId())) {
                        throw new CustomerDoesNotHaveOrderWithSuchId(orderId);
                    }
                    return orderMapper.map(order);
                })
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public OrderCancelledResponseDto cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!Objects.equals(order.getCustomerId(), getCurrentUserId())) {
            throw new CustomerDoesNotHaveOrderWithSuchId(orderId);
        }
        validateOrderStatusIsCancellable(order);
        orderProcessingService.processOrderCancellation(order);
        return new OrderCancelledResponseDto(orderId);
    }

    private void validateOrderStatusIsCancellable(Order order) {
        if (!OrderStatus.CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new OrderCannotBeCancelledException(order.getId(), order.getStatus());
        }
    }

    private List<Product> validateOrderCreationRequestDto(OrderCreationRequestDto orderCreationRequestDto) {
        List<Product> products = productService.checkProductsAvailability(orderCreationRequestDto.productsAmountByProductId().keySet());
        validateOrderIdempotencyKey(orderCreationRequestDto);
        return products;
    }

    private void validateOrderIdempotencyKey(OrderCreationRequestDto orderCreationRequestDto) {
        List<Order> customerOrders = findAllOrdersByCustomerId();
        if (!customerOrders.stream().filter(o -> o.getIdempotencyKey().equals(orderCreationRequestDto.idempotencyKey())).toList().isEmpty()) {
            throw new OrderWithSuchIdempotencyKeyAlreadyExistsException(orderCreationRequestDto.idempotencyKey());
        }
    }

    private List<Order> findAllOrdersByCustomerId() {
        return orderRepository.findAllOrdersByCustomerId(getCurrentUserId());
    }
}
