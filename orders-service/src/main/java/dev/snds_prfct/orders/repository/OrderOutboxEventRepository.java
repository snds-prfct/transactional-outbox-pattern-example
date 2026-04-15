package dev.snds_prfct.orders.repository;

import dev.snds_prfct.orders.constant.OrderOutboxEventStatus;
import dev.snds_prfct.orders.entity.outbox.OrderOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface OrderOutboxEventRepository extends JpaRepository<OrderOutboxEvent, Long> {

    @Query(value = """
            SELECT oe FROM OrderOutboxEvent oe
            WHERE oe.status = 'PENDING'
            ORDER BY oe.createdAt ASC
            LIMIT :batchSize""")
    List<OrderOutboxEvent> findPendingOrderOutboxEventsBatch(int batchSize);

    @Modifying
    @Query("UPDATE OrderOutboxEvent oe SET oe.status = :status, oe.updatedAt = CURRENT_TIMESTAMP WHERE oe.id IN (:ids)")
    void changeOrderOutboxEventStatuses(Set<Long> ids, OrderOutboxEventStatus status);
}
