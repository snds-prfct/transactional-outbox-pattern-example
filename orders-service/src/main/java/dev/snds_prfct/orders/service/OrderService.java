package dev.snds_prfct.orders.service;

import dev.snds_prfct.orders.dto.request.OrderBy;
import dev.snds_prfct.orders.dto.request.OrderCreationRequestDto;
import dev.snds_prfct.orders.dto.request.OrderDirection;
import dev.snds_prfct.orders.dto.response.OrderCancelledResponseDto;
import dev.snds_prfct.orders.dto.response.OrderCreatedResponseDto;
import dev.snds_prfct.orders.dto.response.OrderResponseDto;
import dev.snds_prfct.orders.dto.response.PageableResponse;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.entity.products.Product;
import dev.snds_prfct.orders.exception.CurrentUserDoesNotHaveOrderWithSuchIdException;
import dev.snds_prfct.orders.exception.OrderCannotBeCancelledException;
import dev.snds_prfct.orders.exception.OrderNotFoundException;
import dev.snds_prfct.orders.exception.OrderWithSuchIdempotencyKeyAlreadyExistsException;
import dev.snds_prfct.orders.mapper.OrderMapper;
import dev.snds_prfct.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public PageableResponse<OrderResponseDto> findCustomerOrders(int page, int size, OrderBy orderBy, OrderDirection orderDirection) {
        Page<Order> pageableResult = findAllOrdersByCustomerId(page, size, orderBy, orderDirection);
        List<OrderResponseDto> responseContent = pageableResult.getContent().stream()
                .map(orderMapper::map)
                .toList();

        return PageableResponse.<OrderResponseDto>builder()
                .content(responseContent)
                .pagination(
                        PageableResponse.Pagination.builder()
                                .page(pageableResult.getNumber())
                                .pages(pageableResult.getTotalPages())
                                .isLast(pageableResult.isLast())
                                .build()
                )
                .build();
    }

    public OrderResponseDto findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    if (!Objects.equals(order.getCustomerId(), getCurrentUserId())) {
                        throw new CurrentUserDoesNotHaveOrderWithSuchIdException(orderId);
                    }
                    return orderMapper.map(order);
                })
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public OrderCancelledResponseDto cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!Objects.equals(order.getCustomerId(), getCurrentUserId())) {
            throw new CurrentUserDoesNotHaveOrderWithSuchIdException(orderId);
        }
        validateOrderStatusIsCancellable(order);
        orderProcessingService.processOrderCancellation(orderId);
        return new OrderCancelledResponseDto(orderId);
    }

    private void validateOrderStatusIsCancellable(Order order) {
        if (!order.getStatus().isCancellable()) {
            throw new OrderCannotBeCancelledException(order.getId(), order.getStatus());
        }
    }

    private List<Product> validateOrderCreationRequestDto(OrderCreationRequestDto orderCreationRequestDto) {
        List<Product> products = productService.checkProductsAvailability(orderCreationRequestDto.productsAmountByProductId().keySet());
        validateOrderIdempotencyKey(orderCreationRequestDto);
        return products;
    }

    private void validateOrderIdempotencyKey(OrderCreationRequestDto orderCreationRequestDto) {
        if (orderRepository.existsByCustomerIdAndIdempotencyKey(getCurrentUserId(), orderCreationRequestDto.idempotencyKey())) {
            throw new OrderWithSuchIdempotencyKeyAlreadyExistsException(orderCreationRequestDto.idempotencyKey());
        }
    }

    private Page<Order> findAllOrdersByCustomerId(int page, int size, OrderBy orderBy, OrderDirection orderDirection) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(orderDirection == OrderDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC, orderBy.getValue()));
        return orderRepository.findAllOrdersByCustomerId(getCurrentUserId(), pageable);
    }
}
