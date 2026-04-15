package dev.snds_prfct.orders.kafka.producer;

import dev.snds_prfct.orders.kafka.message.MessageInfo;
import dev.snds_prfct.orders.kafka.message.OrderMessage;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public interface KafkaProducer<K, V> {

    List<CompletableFuture<SendResult<K, V>>> sendMessages(String topic, List<MessageInfo<K, V>> messages);

}
