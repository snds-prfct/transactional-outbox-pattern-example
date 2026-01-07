package dev.snds_prfct.orders.dto.response;

import java.time.Instant;

public record ErrorDetails(
        Instant timestamp,
        int status,
        String error) {

    public static ErrorDetails of(int status, String error) {
        return new ErrorDetails(Instant.now(), status, error);
    }
}
