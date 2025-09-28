package dev.sndsprfct.orders.handler;

import dev.sndsprfct.orders.dto.response.ErrorDetails;
import dev.sndsprfct.orders.exception.CustomerDoesNotHaveOrderWithSuchId;
import dev.sndsprfct.orders.exception.OrderCannotBeCancelledException;
import dev.sndsprfct.orders.exception.OrderNotFoundException;
import dev.sndsprfct.orders.exception.OrderWithSuchIdempotencyKeyAlreadyExistsException;
import dev.sndsprfct.orders.exception.ProductsNotAvailableException;
import dev.sndsprfct.orders.exception.ProductsNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

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

    @ExceptionHandler(CustomerDoesNotHaveOrderWithSuchId.class)
    public ResponseEntity<ErrorDetails> handleCustomerDoesNotHaveOrderWithSuchId(CustomerDoesNotHaveOrderWithSuchId e) {
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
