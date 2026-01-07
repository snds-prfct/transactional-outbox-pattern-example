package dev.snds_prfct.orders.exception;

import java.util.UUID;

public class OrderWithSuchIdempotencyKeyAlreadyExistsException extends RuntimeException {
    private static final String EXCEPTION_MESSAGE_TEMPLATE = "Order with idempotency key '%s' already exists";

    public OrderWithSuchIdempotencyKeyAlreadyExistsException(UUID idempotencyKey) {
        super(EXCEPTION_MESSAGE_TEMPLATE.formatted(idempotencyKey));
    }
}
