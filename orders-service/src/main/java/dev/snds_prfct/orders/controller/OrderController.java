package dev.snds_prfct.orders.controller;

import dev.snds_prfct.orders.dto.request.OrderCreationRequestBody;
import dev.snds_prfct.orders.dto.request.query_parameter.OrderBy;
import dev.snds_prfct.orders.dto.request.query_parameter.OrderDirection;
import dev.snds_prfct.orders.dto.response.OrderCancelledResponseBody;
import dev.snds_prfct.orders.dto.response.OrderCreatedResponseBody;
import dev.snds_prfct.orders.dto.response.OrderResponseDto;
import dev.snds_prfct.orders.dto.response.PageableResponse;
import dev.snds_prfct.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order Operations")
@Validated
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create new order", description = "Creates new order with provided info")
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OrderCreatedResponseBody> createOrder(@Valid @RequestBody OrderCreationRequestBody orderCreationRequestBody) {
        OrderCreatedResponseBody orderCreatedResponseBody = orderService.createOrder(orderCreationRequestBody);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderCreatedResponseBody);
    }

    @Operation(summary = "Find all orders", description = "Returns all user's orders")
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageableResponse<OrderResponseDto>> findOrders(
            @Min(0) @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @Min(10) @Max(100) @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "orderBy", required = false, defaultValue = "createdAt") OrderBy orderBy,
            @RequestParam(name = "orderDirection", required = false, defaultValue = "desc") OrderDirection orderDirection) {
        PageableResponse<OrderResponseDto> orders = orderService.findCustomerOrders(page, size, orderBy, orderDirection);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orders);
    }

    @Operation(summary = "Find order by Id", description = "Returns specific order info by its Id")
    @GetMapping(
            path = "/{orderId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OrderResponseDto> findOrderById(@PathVariable Long orderId) {
        OrderResponseDto order = orderService.findCustomerOrder(orderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(order);
    }

    @Operation(summary = "Cancel order by Id", description = "Cancels order with provided Id")
    @PatchMapping(
            path = "/{orderId}/cancel",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OrderCancelledResponseBody> cancelOrder(@PathVariable Long orderId) {
        OrderCancelledResponseBody orderCancelledResponseBody = orderService.cancelOrder(orderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderCancelledResponseBody);
    }
}
