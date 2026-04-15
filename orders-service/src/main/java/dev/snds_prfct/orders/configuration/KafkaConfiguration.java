package dev.snds_prfct.orders.configuration;

import dev.snds_prfct.orders.kafka.property.OutboxEventsCancelledOrdersTopicProperties;
import dev.snds_prfct.orders.kafka.property.OutboxEventsCreatedOrdersTopicProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private final OutboxEventsCreatedOrdersTopicProperties outboxEventsCreatedOrdersTopicProperties;
    private final OutboxEventsCancelledOrdersTopicProperties outboxEventsCancelledOrdersTopicProperties;

    @Bean
    public NewTopic createdOrdersTopic() {
        return TopicBuilder
                .name(outboxEventsCreatedOrdersTopicProperties.topic())
                .partitions(outboxEventsCreatedOrdersTopicProperties.partitions())
                .build();
    }

    @Bean
    public NewTopic cancelledOrdersTopic() {
        return TopicBuilder
                .name(outboxEventsCancelledOrdersTopicProperties.topic())
                .partitions(outboxEventsCancelledOrdersTopicProperties.partitions())
                .build();
    }
}
