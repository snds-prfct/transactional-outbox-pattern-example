package dev.snds_prfct.orders.exception;

public class CustomerDoesNotHaveOrderWithSuchId extends RuntimeException {

    public static final String EXCEPTION_MESSAGE_TEMPLATE = "Customer does not have order with id '%d'";

    public CustomerDoesNotHaveOrderWithSuchId(Long orderId) {
        super(EXCEPTION_MESSAGE_TEMPLATE.formatted(orderId));
    }
}
