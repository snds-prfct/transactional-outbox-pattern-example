package dev.snds_prfct.orders.message;

public record MessageInfo<K, V>(
        K key,
        V value) {
}
