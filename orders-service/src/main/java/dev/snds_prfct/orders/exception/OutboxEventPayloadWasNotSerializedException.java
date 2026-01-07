package dev.snds_prfct.orders.exception;

public class OutboxEventPayloadWasNotSerializedException extends RuntimeException {
    public OutboxEventPayloadWasNotSerializedException(Exception e) {
        super(e);
    }
}
