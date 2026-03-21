package dev.snds_prfct.orders.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.snds_prfct.orders.constant.OrderOutboxEventStatus;
import dev.snds_prfct.orders.constant.OrderOutboxEventType;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.entity.outbox.OrderOutboxEvent;
import dev.snds_prfct.orders.exception.OutboxEventPayloadWasNotSerializedException;
import dev.snds_prfct.orders.repository.OrderOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderOutboxService {

    @Value("${orders.outbox.readBatchSize}")
    private Integer outboxReadBatchSize;

    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final ObjectMapper objectMapper;

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

    private String serializePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new OutboxEventPayloadWasNotSerializedException(e);
        }
    }
}
