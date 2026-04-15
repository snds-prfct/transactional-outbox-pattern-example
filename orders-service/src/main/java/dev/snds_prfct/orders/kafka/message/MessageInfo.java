package dev.snds_prfct.orders.kafka.message;

public record MessageInfo<K, V>(
        K key,
        V value) {
}
