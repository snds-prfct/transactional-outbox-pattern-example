package dev.snds_prfct.orders.kafka.producer;

import dev.snds_prfct.orders.kafka.message.MessageInfo;
import dev.snds_prfct.orders.kafka.message.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaProducer implements KafkaProducer<Long, OrderMessage> {

    private final KafkaTemplate<Long, OrderMessage> kafkaTemplate;

    public List<CompletableFuture<SendResult<Long, OrderMessage>>> sendMessages(String topic, List<MessageInfo<Long, OrderMessage>> messages) {
        if (messages == null || messages.isEmpty()) {
            log.warn("No messages provided to send. Topic: '{}'", topic);
            return Collections.emptyList();
        }
        log.debug("Sending {} messages to '{}' topic", messages.size(), topic);
        return messages.stream()
                .map(message -> kafkaTemplate.send(topic, message.key(), message.value()))
                .toList();
    }
}
