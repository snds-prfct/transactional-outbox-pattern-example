package dev.snds_prfct.orders.handler;

import dev.snds_prfct.orders.dto.response.ErrorDetails;
import dev.snds_prfct.orders.exception.CurrentUserDoesNotHaveOrderWithSuchIdException;
import dev.snds_prfct.orders.exception.OrderCannotBeCancelledException;
import dev.snds_prfct.orders.exception.OrderNotFoundException;
import dev.snds_prfct.orders.exception.OrderWithSuchIdempotencyKeyAlreadyExistsException;
import dev.snds_prfct.orders.exception.ProductsNotAvailableException;
import dev.snds_prfct.orders.exception.ProductsNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductsNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleProductsNotFoundException(ProductsNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorDetails.of(HttpStatus.CONFLICT.value(), e.getMessage()));
    }

    @ExceptionHandler(ProductsNotAvailableException.class)
    public ResponseEntity<ErrorDetails> handleProductsNotAvailableException(ProductsNotAvailableException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorDetails.of(HttpStatus.CONFLICT.value(), e.getMessage()));
    }

    @ExceptionHandler(OrderWithSuchIdempotencyKeyAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleOrderWithSuchIdempotencyKeyAlreadyExistsException(OrderWithSuchIdempotencyKeyAlreadyExistsException e) {
        return ResponseEntity
                .badRequest()
                .body(ErrorDetails.of(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleOrderNotFoundException(OrderNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorDetails.of(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler(CurrentUserDoesNotHaveOrderWithSuchIdException.class)
    public ResponseEntity<ErrorDetails> handleCustomerDoesNotHaveOrderWithSuchId(CurrentUserDoesNotHaveOrderWithSuchIdException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorDetails.of(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler(OrderCannotBeCancelledException.class)
    public ResponseEntity<ErrorDetails> handleOrderCannotBeCancelledException(OrderCannotBeCancelledException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorDetails.of(HttpStatus.CONFLICT.value(), e.getMessage()));
    }
}
