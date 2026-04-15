package dev.snds_prfct.orders.exception;

public class OutboxEventPayloadWasNotSerializedException extends InternalServerException {
    public OutboxEventPayloadWasNotSerializedException(Exception e) {
        super(e);
    }
}
