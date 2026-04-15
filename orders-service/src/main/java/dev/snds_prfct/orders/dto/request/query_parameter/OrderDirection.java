package dev.snds_prfct.orders.dto.request.query_parameter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderDirection {
    ASC("asc"),
    DESC("desc");

    public final String value;
}
