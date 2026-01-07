package dev.snds_prfct.orders.service;

import dev.snds_prfct.orders.constant.OrderStatus;
import dev.snds_prfct.orders.constant.OutboxEventType;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final OrderRepository orderRepository;
    private final OutboxService outboxService;

    @Transactional
    public Long processOrderCreation(Order order) {
        Order savedOrder = orderRepository.save(order);
        outboxService.saveOutboxEvent(savedOrder, OutboxEventType.ORDER_CREATED);
        return savedOrder.getId();
    }

    @Transactional
    public void processOrderCancellation(Order order) {
        orderRepository.updateOrderStatus(order.getId(), OrderStatus.CANCELED);
        outboxService.saveOutboxEvent(order, OutboxEventType.ORDER_CANCELLED);
    }
}
