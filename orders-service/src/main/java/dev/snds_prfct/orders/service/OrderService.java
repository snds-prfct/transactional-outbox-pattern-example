package dev.snds_prfct.orders.service;

import dev.snds_prfct.orders.dto.request.OrderCreationRequestBody;
import dev.snds_prfct.orders.dto.request.query_parameter.OrderBy;
import dev.snds_prfct.orders.dto.request.query_parameter.OrderDirection;
import dev.snds_prfct.orders.dto.response.OrderCancelledResponseBody;
import dev.snds_prfct.orders.dto.response.OrderCreatedResponseBody;
import dev.snds_prfct.orders.dto.response.OrderResponseDto;
import dev.snds_prfct.orders.dto.response.PageableResponse;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.entity.products.Product;
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

import static dev.snds_prfct.orders.security.PrincipalUtils.getCurrentUserId;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderProcessingService orderProcessingService;

    public OrderCreatedResponseBody createOrder(OrderCreationRequestBody orderCreationRequestBody) {
        validateOrderIdempotencyKey(orderCreationRequestBody);
        List<Product> products = productService.findProducts(orderCreationRequestBody.productsAmountByProductId().keySet());
        Order order = orderMapper.map(orderCreationRequestBody, products);
        Long createdOrderId = orderProcessingService.processOrderCreation(order);
        return new OrderCreatedResponseBody(createdOrderId);
    }

    public PageableResponse<OrderResponseDto> findCustomerOrders(int page, int size, OrderBy orderBy, OrderDirection orderDirection) {
        Page<Order> pageableResult = findAllOrdersByCustomerId(page, size, orderBy, orderDirection);
        return orderMapper.map(pageableResult);
    }

    public OrderResponseDto findCustomerOrder(Long orderId) {
        return orderRepository.findByIdAndCustomerId(orderId, getCurrentUserId())
                .map(orderMapper::map)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public OrderCancelledResponseBody cancelOrder(Long orderId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, getCurrentUserId())
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        validateOrderStatusIsCancellable(order);
        orderProcessingService.processOrderCancellation(orderId);
        return new OrderCancelledResponseBody(orderId);
    }

    private void validateOrderStatusIsCancellable(Order order) {
        if (!order.getStatus().isCancellable()) {
            throw new OrderCannotBeCancelledException(order.getId(), order.getStatus());
        }
    }

    private void validateOrderIdempotencyKey(OrderCreationRequestBody orderCreationRequestBody) {
        if (orderRepository.existsByCustomerIdAndIdempotencyKey(getCurrentUserId(), orderCreationRequestBody.idempotencyKey())) {
            throw new OrderWithSuchIdempotencyKeyAlreadyExistsException(orderCreationRequestBody.idempotencyKey());
        }
    }

    private Page<Order> findAllOrdersByCustomerId(int page, int size, OrderBy orderBy, OrderDirection orderDirection) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(orderDirection == OrderDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC, orderBy.getValue()));
        return orderRepository.findAllOrdersByCustomerId(getCurrentUserId(), pageable);
    }
}
