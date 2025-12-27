package dev.snds_prfct.orders.repository;

import dev.snds_prfct.orders.entity.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
}
