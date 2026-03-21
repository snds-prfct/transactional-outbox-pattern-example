package dev.snds_prfct.orders.producer;

import dev.snds_prfct.orders.message.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class OutboxEventKafkaProducer implements KafkaProducer<Long, String> {

    private final KafkaTemplate<Long, String> kafkaTemplate;


    public List<CompletableFuture<SendResult<Long, String>>> sendMessages(String topic, List<Message<Long, String>> messages) {
        List<CompletableFuture<SendResult<Long, String>>> completableFutures = new ArrayList<>();
        for (Message<Long, String> keyValue : messages) {
            completableFutures.add(kafkaTemplate.send(topic, keyValue.key(), keyValue.data()));
        }
        return completableFutures;
    }
}
