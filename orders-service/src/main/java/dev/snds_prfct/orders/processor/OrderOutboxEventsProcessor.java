package dev.snds_prfct.orders.processor;

import dev.snds_prfct.orders.configuration.OutboxEventsCancelledOrdersTopicProperties;
import dev.snds_prfct.orders.configuration.OutboxEventsCreatedOrdersTopicProperties;
import dev.snds_prfct.orders.constant.OrderOutboxEventStatus;
import dev.snds_prfct.orders.mapper.OrderMapper;
import dev.snds_prfct.orders.mapper.OrderMessageMapper;
import dev.snds_prfct.orders.message.MessageInfo;
import dev.snds_prfct.orders.message.OrderMessage;
import dev.snds_prfct.orders.producer.OrderKafkaProducer;
import dev.snds_prfct.orders.service.OrderOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxEventsProcessor {

    private final OrderOutboxService orderOutboxService;
    private final OrderKafkaProducer orderKafkaProducer;
    private final OutboxEventsCreatedOrdersTopicProperties outboxEventsCreatedOrdersTopicProperties;
    private final OutboxEventsCancelledOrdersTopicProperties outboxEventsCancelledOrdersTopicProperties;
    private final OrderMapper orderMapper;
    private final OrderMessageMapper orderMessageMapper;

    @Scheduled(fixedDelayString = "${orders.outbox.processing.fixedDelayMs}")
    void processOutboxEvents() {
        List<MessageInfo<Long, OrderMessage>> messages = findNextPendingMessages();

        if (messages.isEmpty()) {
            log.debug("No order outbox events with status to process");
            return;
        }

        log.info("Processing next batch ({}) of order events from outbox", messages.size());

        messages.stream()
                .collect(Collectors.groupingBy(messageInfo -> messageInfo.value().type()))
                .forEach(((orderEventType, messageInfos) -> sendMessages(resolveTopic(orderEventType), messageInfos)));
    }

    private String resolveTopic(OrderMessage.OrderEventType orderEventType) {
        return switch (orderEventType) {
            case ORDER_CREATED -> outboxEventsCreatedOrdersTopicProperties.topic();
            case ORDER_CANCELLED -> outboxEventsCancelledOrdersTopicProperties.topic();
        };
    }

    private List<MessageInfo<Long, OrderMessage>> findNextPendingMessages() {
        log.debug("Searching new pending order events in outbox");
        return orderOutboxService.findPendingOrderOutboxEventsBatch().stream()
                .map(event -> new MessageInfo<>(event.getId(), orderMessageMapper.map(orderMapper.map(event.getPayload()))))
                .toList();
    }

    private void sendMessages(String topic, List<MessageInfo<Long, OrderMessage>> messages) {
        List<CompletableFuture<SendResult<Long, OrderMessage>>> sentMessages = orderKafkaProducer.sendMessages(topic, messages);
        waitMessagesAreSent(topic, sentMessages);
        handleSentMessagesStatus(topic, sentMessages);
    }

    private void waitMessagesAreSent(String topic, List<CompletableFuture<SendResult<Long, OrderMessage>>> completableFutures) {
        log.debug("Waiting when all {} messages will be sent to {} topic", completableFutures.size(), topic);
        while (true) {
            boolean areMessagesSent = true;
            for (CompletableFuture<SendResult<Long, OrderMessage>> future : completableFutures) {
                if (!future.isDone()) {
                    areMessagesSent = false;
                    break;
                }
            }
            if (areMessagesSent) {
                break;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.debug("The process of sending messages about new orders from outbox to topic '{}' was completed", topic);
    }

    private void handleSentMessagesStatus(String topic, List<CompletableFuture<SendResult<Long, OrderMessage>>> completableFutures) {
        List<SendResult<Long, OrderMessage>> completedExceptionally = new ArrayList<>();
        List<SendResult<Long, OrderMessage>> completedSuccessfully = new ArrayList<>();

        for (CompletableFuture<SendResult<Long, OrderMessage>> completableFuture : completableFutures) {
            if (completableFuture.isCompletedExceptionally() || completableFuture.isCancelled()) {
                try {
                    completedExceptionally.add(completableFuture.get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    completedSuccessfully.add(completableFuture.get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (!completedExceptionally.isEmpty()) {
            log.error("Not all messages about orders from outbox were sent successfully ({}/{}) to '{}' topic",
                    completedExceptionally.size(), completableFutures.size(), topic);
        }

        if (!completedSuccessfully.isEmpty()) {
            log.info("{}/{} messages about orders from outbox were sent successfully to '{}' topic",
                    completedSuccessfully.size(), completableFutures.size(), topic);
            Set<Long> ids = completedSuccessfully.stream()
                    .map(m -> m.getProducerRecord().key())
                    .collect(Collectors.toSet());
            orderOutboxService.changeOrderOutboxEventStatuses(ids, OrderOutboxEventStatus.SENT);
        }
    }
}
