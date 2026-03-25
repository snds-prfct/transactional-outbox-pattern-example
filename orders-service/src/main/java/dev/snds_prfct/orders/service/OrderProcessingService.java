package dev.snds_prfct.orders.service;

import dev.snds_prfct.orders.constant.OrderOutboxEventType;
import dev.snds_prfct.orders.constant.OrderStatus;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final OrderRepository orderRepository;
    private final OrderOutboxService orderOutboxService;

    @Transactional
    public Long processOrderCreation(Order order) {
        Order savedOrder = orderRepository.save(order);
        orderOutboxService.saveOutboxEvent(savedOrder, OrderOutboxEventType.ORDER_CREATED);
        return savedOrder.getId();
    }

    @Transactional
    public void processOrderCancellation(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.CANCELED);
        orderOutboxService.saveOutboxEvent(order, OrderOutboxEventType.ORDER_CANCELLED);
    }
}
