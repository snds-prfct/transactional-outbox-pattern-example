package dev.snds_prfct.orders.configuration.converter;

import dev.snds_prfct.orders.dto.request.query_parameter.OrderBy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToOrderByConverter implements Converter<String, OrderBy> {
    @Override
    public OrderBy convert(String source) {
        return OrderBy.mappings.get(source);
    }
}
