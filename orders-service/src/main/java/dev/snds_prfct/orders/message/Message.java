package dev.snds_prfct.orders.message;

public record Message<K, V>(K key, V data) {
}
