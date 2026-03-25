package dev.snds_prfct.orders.producer;

import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;


public interface KafkaProducer<K, V> {

    CompletableFuture<SendResult<K, V>> sendMessage(String topic, K key, V message);

}
