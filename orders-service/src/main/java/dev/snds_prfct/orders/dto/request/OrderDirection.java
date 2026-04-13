package dev.snds_prfct.orders.dto.request;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderDirection {
    ASC("asc"),
    DESC("desc");

    public final String value;
}
