package dev.snds_prfct.orders.configuration.converter;

import dev.snds_prfct.orders.dto.request.query_parameter.OrderDirection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToOrderDirectionConverter implements Converter<String, OrderDirection> {
    @Override
    public OrderDirection convert(String source) {
        return OrderDirection.valueOf(source.toUpperCase());
    }
}
