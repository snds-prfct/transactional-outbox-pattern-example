package dev.snds_prfct.orders.exception;

public class OrderNotFoundException extends RuntimeException {
    private static final String EXCEPTION_MESSAGE_TEMPLATE = "Order with id '%d' not found";

    public OrderNotFoundException(Long orderId) {
        super(EXCEPTION_MESSAGE_TEMPLATE.formatted(orderId));
    }
}
