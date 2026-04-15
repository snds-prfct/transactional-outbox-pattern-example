package dev.snds_prfct.orders.dto.request.query_parameter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum OrderBy {
    CREATED_AT("createdAt");

    private final String value;

    public static final Map<String, OrderBy> mappings = Map.of(CREATED_AT.value, CREATED_AT);

}
