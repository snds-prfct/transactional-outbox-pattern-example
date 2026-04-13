package dev.snds_prfct.orders.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PageableResponse<T>(
        List<T> content,
        Pagination pagination) {

    @Builder
    public record Pagination(
            int page,
            int pages,
            boolean isLast) {
    }
}
