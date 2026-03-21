package dev.snds_prfct.orders.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "orders.outbox.kafka.topics.cancelled-orders")
public record OutboxEventsCancelledOrdersTopicProperties(
        String topic,
        Integer partitions) {
}
