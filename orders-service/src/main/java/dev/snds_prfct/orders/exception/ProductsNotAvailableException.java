package dev.snds_prfct.orders.exception;

import java.util.Collection;

public class ProductsNotAvailableException extends RuntimeException {

    private static final String EXCEPTION_MESSAGE_TEMPLATE = "Products with ids %s are not available";

    public ProductsNotAvailableException(Collection<Long> productIds) {
        super(EXCEPTION_MESSAGE_TEMPLATE.formatted(productIds));
    }
}
