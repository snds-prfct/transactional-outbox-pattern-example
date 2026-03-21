package dev.snds_prfct.orders.exception;

public class CurrentUserDoesNotHaveOrderWithSuchIdException extends RuntimeException {

    public static final String EXCEPTION_MESSAGE_TEMPLATE = "Current user does not have order with id '%d'";

    public CurrentUserDoesNotHaveOrderWithSuchIdException(Long orderId) {
        super(EXCEPTION_MESSAGE_TEMPLATE.formatted(orderId));
    }
}
