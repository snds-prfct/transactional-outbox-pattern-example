package dev.snds_prfct.orders.controller;

import dev.snds_prfct.orders.dto.response.OrderCancelledResponseDto;
import dev.snds_prfct.orders.dto.request.OrderCreationRequestDto;
import dev.snds_prfct.orders.dto.response.OrderCreatedResponseDto;
import dev.snds_prfct.orders.dto.response.OrderResponseDto;
import dev.snds_prfct.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderCreatedResponseDto> createOrder(@Valid @RequestBody OrderCreationRequestDto orderCreationRequestDto) {
        OrderCreatedResponseDto orderCreatedResponseDto = orderService.createOrder(orderCreationRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderCreatedResponseDto);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDto>> findOrders() {
        List<OrderResponseDto> orders = orderService.findOrders();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orders);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDto> findOrder(@PathVariable Long orderId) {
        OrderResponseDto order = orderService.findOrder(orderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(order);
    }

    @PatchMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderCancelledResponseDto> cancelOrder(@PathVariable Long orderId) {
        OrderCancelledResponseDto orderCancelledResponseDto = orderService.cancelOrder(orderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderCancelledResponseDto);
    }
}
