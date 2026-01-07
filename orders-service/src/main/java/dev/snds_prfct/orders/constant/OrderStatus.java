package dev.snds_prfct.orders.constant;

import java.util.List;

public enum OrderStatus {
    PENDING, PROCESSING, DELIVERED, COMPLETED, CANCELED;

    static public final List<OrderStatus> CANCELLABLE_STATUSES = List.of(PENDING, PROCESSING);
}
