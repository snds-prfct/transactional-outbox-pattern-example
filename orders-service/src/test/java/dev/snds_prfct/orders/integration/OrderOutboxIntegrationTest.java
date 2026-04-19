package dev.snds_prfct.orders.integration;

import dev.snds_prfct.orders.constant.OrderOutboxEventStatus;
import dev.snds_prfct.orders.constant.OrderOutboxEventType;
import dev.snds_prfct.orders.entity.outbox.OrderOutboxEvent;
import dev.snds_prfct.orders.kafka.message.OrderMessage;
import dev.snds_prfct.orders.processor.OrderOutboxEventsProcessor;
import dev.snds_prfct.orders.test_component.DaoUtils;
import dev.snds_prfct.orders.test_component.TestcontainersConfiguration;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@EmbeddedKafka(
        topics = {"${orders.outbox.kafka.topics.created-orders.topic}"},
        partitions = 1
)
public class OrderOutboxIntegrationTest {

    @Autowired
    private OrderOutboxEventsProcessor orderOutboxEventsProcessor;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private DaoUtils daoUtils;

    @Value("${orders.outbox.kafka.topics.created-orders.topic}")
    private String topic;

    @Test
    @Sql(value = "/sql/init-order-outbox-events.sql")
    void testOrderOutboxEventsSendingToKafka() {
        // given
        Consumer<Long, OrderMessage> consumer = getOrderMessageConsumer();

        // when
        orderOutboxEventsProcessor.processOutboxEvents();

        // then
        this.embeddedKafka.consumeFromAnEmbeddedTopic(consumer, topic);

        ConsumerRecords<Long, OrderMessage> replies = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
        ArrayList<OrderMessage> orderMessages = new ArrayList<>();
        replies.records(topic).forEach(r -> orderMessages.add(r.value()));

        assertThat(orderMessages)
                .isNotEmpty()
                .hasSize(2)
                .extracting("type", "source")
                .containsExactlyInAnyOrder(
                        tuple(OrderMessage.OrderEventType.ORDER_CREATED, "orders-service"),
                        tuple(OrderMessage.OrderEventType.ORDER_CREATED, "orders-service"));

        List<OrderOutboxEvent> allOrderOutboxEvents = daoUtils.findAllOrderOutboxEvents();
        assertThat(allOrderOutboxEvents)
                .hasSize(2)
                .extracting(OrderOutboxEvent::getType, OrderOutboxEvent::getStatus, OrderOutboxEvent::getUpdatedAt)
                .allSatisfy(tuple -> {
                    List<Object> list = tuple.toList();
                    assertThat(list.get(0)).isEqualTo(OrderOutboxEventType.ORDER_CREATED);
                    assertThat(list.get(1)).isEqualTo(OrderOutboxEventStatus.SENT);
                    assertThat(list.get(2)).isNotNull();
                });
    }

    private Consumer<Long, OrderMessage> getOrderMessageConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "testGroup");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10");
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.LongDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JacksonJsonDeserializer");
        consumerProps.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");

        return new DefaultKafkaConsumerFactory<Long, OrderMessage>(consumerProps)
                .createConsumer();
    }
}
