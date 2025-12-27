package dev.snds_prfct.orders.exception;

import java.util.Collection;

public class ProductsNotFoundException extends RuntimeException {
    private static final String EXCEPTION_MESSAGE_TEMPLATE = "Products with ids %s not found";

    public ProductsNotFoundException(Collection<Long> productIds) {
        super(EXCEPTION_MESSAGE_TEMPLATE.formatted(productIds));
    }
}
