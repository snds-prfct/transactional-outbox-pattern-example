package dev.snds_prfct.orders.processor;

import dev.snds_prfct.orders.configuration.OutboxEventsCreatedOrdersTopicProperties;
import dev.snds_prfct.orders.constant.OrderOutboxEventStatus;
import dev.snds_prfct.orders.message.Message;
import dev.snds_prfct.orders.producer.OutboxEventKafkaProducer;
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
public class CreatedOrdersOutboxEventsProcessor {

    private final OrderOutboxService orderOutboxService;
    private final OutboxEventKafkaProducer outboxEventKafkaProducer;
    private final OutboxEventsCreatedOrdersTopicProperties outboxProducerProperties;

    @Scheduled(fixedDelayString = "${orders.outbox.processing.fixedDelayMs}")
    void processOutboxEvents() {
        List<Message<Long, String>> messages = findNextPendingMessages();

        if (messages.isEmpty()) {
            log.debug("No orders outbox events with status to process");
            return;
        }

        log.info("Processing next batch ({}) of new order events from outbox", messages.size());

        List<CompletableFuture<SendResult<Long, String>>> completableFutures = sendMessages(messages);

        waitMessagesAreSent(completableFutures);
        handleSentMessagesStatus(completableFutures);

        log.info("{} events from outbox for new orders were processed", messages.size());
    }

    private List<Message<Long, String>> findNextPendingMessages() {
        log.debug("Searching new pending order events in outbox");
        return orderOutboxService.findPendingOrderOutboxEventsBatch().stream()
                .map(event -> new Message<>(event.getId(), event.getPayload()))
                .toList();
    }

    private List<CompletableFuture<SendResult<Long, String>>> sendMessages(List<Message<Long, String>> messages) {
        log.debug("Sending {} messages about new orders to '{}' topic", messages.size(), outboxProducerProperties.topic());
        return outboxEventKafkaProducer.sendMessages(outboxProducerProperties.topic(), messages);
    }

    private void waitMessagesAreSent(List<CompletableFuture<SendResult<Long, String>>> completableFutures) {
        log.debug("Waiting when all messages  will be sent");
        while (true) {
            boolean areMessagesSent = true;
            for (CompletableFuture<SendResult<Long, String>> future : completableFutures) {
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
        log.debug("THe process of sending messages about new orders from outbox was completed");
    }

    private void handleSentMessagesStatus(List<CompletableFuture<SendResult<Long, String>>> completableFutures) {
        List<SendResult<Long, String>> completedExceptionally = new ArrayList<>();
        List<SendResult<Long, String>> completedSuccessfully = new ArrayList<>();

        for (CompletableFuture<SendResult<Long, String>> completableFuture : completableFutures) {
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
            log.error("Not all messages about new orders from outbox were sent successfully ({}/{}) to '{}' topic",
                    completedExceptionally.size(), completableFutures.size(), outboxProducerProperties.topic());
        }

        if (!completedSuccessfully.isEmpty()) {
            log.info("{}/{} messages about new orders from outbox were sent successfully to '{}' topic",
                    completedSuccessfully.size(), completableFutures.size(), outboxProducerProperties.topic());
            Set<Long> ids = completedSuccessfully.stream()
                    .map(m -> m.getProducerRecord().key())
                    .collect(Collectors.toSet());
            orderOutboxService.changeOrderOutboxEventStatuses(ids, OrderOutboxEventStatus.SENT);
        }
    }
}
