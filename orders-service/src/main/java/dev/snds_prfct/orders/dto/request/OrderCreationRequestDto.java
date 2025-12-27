package dev.snds_prfct.orders.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record OrderCreationRequestDto(
        @NotNull
        UUID idempotencyKey,
        @NotEmpty
        Map<Long, Integer> productsAmountByProductId,
        @NotNull
        String deliveryAddress) {
}
