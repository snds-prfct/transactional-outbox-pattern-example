package dev.snds_prfct.orders.entity.outbox;

import dev.snds_prfct.orders.constant.OutboxEventStatus;
import dev.snds_prfct.orders.constant.OutboxEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(schema = "outbox", name = "outbox_events")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private OutboxEventType type;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OutboxEventStatus status = OutboxEventStatus.PENDING;

    @Column(name = "payload")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutboxEvent that = (OutboxEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
