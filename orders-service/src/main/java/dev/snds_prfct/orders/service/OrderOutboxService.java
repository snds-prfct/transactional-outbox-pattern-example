package dev.snds_prfct.orders.service;

import dev.snds_prfct.orders.constant.OrderOutboxEventStatus;
import dev.snds_prfct.orders.constant.OrderOutboxEventType;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.entity.outbox.OrderOutboxEvent;
import dev.snds_prfct.orders.exception.OutboxEventPayloadWasNotSerializedException;
import dev.snds_prfct.orders.repository.OrderOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderOutboxService {

    @Value("${orders.outbox.readBatchSize}")
    private int outboxReadBatchSize;

    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final JsonMapper jsonMapper;

    public List<OrderOutboxEvent> findPendingOrderOutboxEventsBatch() {
        return orderOutboxEventRepository.findPendingOrderOutboxEventsBatch(outboxReadBatchSize);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void saveOutboxEvent(Order payload, OrderOutboxEventType orderOutboxEventType) {
        OrderOutboxEvent orderOutboxEvent = new OrderOutboxEvent();
        orderOutboxEvent.setType(orderOutboxEventType);
        orderOutboxEvent.setPayload(serializePayload(payload));
        orderOutboxEventRepository.save(orderOutboxEvent);
    }

    @Transactional
    public void changeOrderOutboxEventStatuses(Set<Long> ids, OrderOutboxEventStatus status) {
        orderOutboxEventRepository.changeOrderOutboxEventStatuses(ids, status);
    }

    private String serializePayload(Order order) {
        try {
            return jsonMapper.writeValueAsString(order);
        } catch (JacksonException e) {
            log.error("Failed to serialize order into JSON format for order outbox event payload. Order: {}", order.toString(), e);
            throw new OutboxEventPayloadWasNotSerializedException(e);
        }
    }
}
