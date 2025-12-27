package dev.snds_prfct.orders.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.snds_prfct.orders.constant.OutboxEventType;
import dev.snds_prfct.orders.entity.outbox.OutboxEvent;
import dev.snds_prfct.orders.exception.OutboxEventPayloadWasNotSerializedException;
import dev.snds_prfct.orders.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.SUPPORTS)
    public void saveOutboxEvent(Object payload, OutboxEventType outboxEventType) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setType(outboxEventType);
        outboxEvent.setPayload(serializePayload(payload));
        outboxEventRepository.save(outboxEvent);
    }

    private String serializePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new OutboxEventPayloadWasNotSerializedException(e);
        }
    }
}
