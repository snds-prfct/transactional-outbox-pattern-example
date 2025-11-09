package dev.sndsprfct.orders.repository;

import dev.sndsprfct.orders.entity.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
            SELECT oe FROM OutboxEvent oe
            WHERE oe.status = 'PENDING'
            ORDER BY oe.createdAt ASC
            LIMIT :batchSize""")
    List<OutboxEvent> findOutboxEventsBatch(Integer batchSize);
}
