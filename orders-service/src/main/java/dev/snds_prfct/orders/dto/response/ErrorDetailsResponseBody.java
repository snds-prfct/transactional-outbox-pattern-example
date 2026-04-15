package dev.snds_prfct.orders.dto.response;

import java.time.Instant;

public record ErrorDetailsResponseBody(
        Instant timestamp,
        int status,
        String error) {

    public static ErrorDetailsResponseBody of(int status, String error) {
        return new ErrorDetailsResponseBody(Instant.now(), status, error);
    }
}
