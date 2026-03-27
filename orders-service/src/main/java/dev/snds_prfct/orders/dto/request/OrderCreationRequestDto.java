package dev.snds_prfct.orders.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

@Schema(title = "Request body for order creation request")
public record OrderCreationRequestDto(
        @Schema(example = "c25a7996-bd20-49e6-b79c-d358e4532091")
        @NotNull
        UUID idempotencyKey,
        @Schema(example = "{\"1\": 2}")
        @NotEmpty
        Map<Long, Integer> productsAmountByProductId,
        @Schema(example = "Country, City, Street, Building")
        @NotNull
        String deliveryAddress) {
}
