package dev.snds_prfct.orders.exception.handler;

import dev.snds_prfct.orders.dto.response.ErrorDetailsResponseBody;
import dev.snds_prfct.orders.exception.InternalServerException;
import dev.snds_prfct.orders.exception.OrderCannotBeCancelledException;
import dev.snds_prfct.orders.exception.OrderNotFoundException;
import dev.snds_prfct.orders.exception.OrderWithSuchIdempotencyKeyAlreadyExistsException;
import dev.snds_prfct.orders.exception.ProductsNotAvailableException;
import dev.snds_prfct.orders.exception.ProductsNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Locale;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorDetailsResponseBody> handleInternalServerException(InternalServerException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDetailsResponseBody.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        messageSource.getMessage("errors.internalServerError", null, Locale.ROOT)));
    }

    @ExceptionHandler(ProductsNotFoundException.class)
    public ResponseEntity<ErrorDetailsResponseBody> handleProductsNotFoundException(ProductsNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorDetailsResponseBody.of(
                        HttpStatus.CONFLICT.value(),
                        messageSource.getMessage("errors.productsNotFound", null, Locale.ROOT)));
    }

    @ExceptionHandler(ProductsNotAvailableException.class)
    public ResponseEntity<ErrorDetailsResponseBody> handleProductsNotAvailableException(ProductsNotAvailableException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorDetailsResponseBody.of(
                        HttpStatus.CONFLICT.value(),
                        messageSource.getMessage("errors.productsNotAvailable", null, Locale.ROOT)));
    }

    @ExceptionHandler(OrderWithSuchIdempotencyKeyAlreadyExistsException.class)
    public ResponseEntity<ErrorDetailsResponseBody> handleOrderWithSuchIdempotencyKeyAlreadyExistsException(OrderWithSuchIdempotencyKeyAlreadyExistsException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .badRequest()
                .body(ErrorDetailsResponseBody.of(
                        HttpStatus.BAD_REQUEST.value(),
                        messageSource.getMessage("errors.orderHasAlreadyBeenCreated", null, Locale.ROOT)));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorDetailsResponseBody> handleOrderNotFoundException(OrderNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorDetailsResponseBody.of(
                        HttpStatus.NOT_FOUND.value(),
                        messageSource.getMessage("errors.orderNotFound", null, Locale.ROOT)));
    }

    @ExceptionHandler(OrderCannotBeCancelledException.class)
    public ResponseEntity<ErrorDetailsResponseBody> handleOrderCannotBeCancelledException(OrderCannotBeCancelledException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorDetailsResponseBody.of(
                        HttpStatus.CONFLICT.value(),
                        messageSource.getMessage("errors.orderCannotBeCancelled", null, Locale.ROOT)));
    }
}
