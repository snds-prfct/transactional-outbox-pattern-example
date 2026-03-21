package dev.snds_prfct.orders.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "orders.outbox.kafka.topics.created-orders")
public record OutboxEventsCreatedOrdersTopicProperties(
        String topic,
        Integer partitions) {
}
