package dev.snds_prfct.orders.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrderStatus {
    PENDING(true),
    PROCESSING(true),
    DELIVERED(false),
    COMPLETED(false),
    CANCELED(false);

    private final boolean isCancellable;

}
