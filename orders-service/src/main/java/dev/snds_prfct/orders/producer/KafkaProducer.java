package dev.snds_prfct.orders.producer;

import dev.snds_prfct.orders.message.Message;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public interface KafkaProducer<K, V> {

    List<CompletableFuture<SendResult<K, V>>> sendMessages(String topic, List<Message<K, V>> messages);

}
