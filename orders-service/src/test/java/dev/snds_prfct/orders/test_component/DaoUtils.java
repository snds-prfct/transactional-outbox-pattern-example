package dev.snds_prfct.orders.test_component;

import dev.snds_prfct.orders.constant.OrderOutboxEventStatus;
import dev.snds_prfct.orders.constant.OrderOutboxEventType;
import dev.snds_prfct.orders.constant.OrderStatus;
import dev.snds_prfct.orders.entity.orders.Order;
import dev.snds_prfct.orders.entity.orders.OrderItem;
import dev.snds_prfct.orders.entity.outbox.OrderOutboxEvent;
import dev.snds_prfct.orders.entity.products.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DaoUtils {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setCustomerId(rs.getLong("customer_id"));
        order.setIdempotencyKey(rs.getObject("idempotency_key", UUID.class));
        order.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        return order;
    };

    private final RowMapper<OrderItem> orderItemsRowMapper = (rs, rowNum) -> {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(rs.getLong("id"));
        orderItem.setOrder(Order.builder().id(rs.getLong("order_id")).build());
        orderItem.setProduct(Product.builder().id(rs.getLong("product_id")).build());
        orderItem.setQuantity(rs.getInt("quantity"));
        orderItem.setUnitPrice(rs.getLong("unit_price"));
        return orderItem;
    };

    private final RowMapper<OrderOutboxEvent> orderOutboxEventsRowMapper = (rs, rowNum) -> {
        OrderOutboxEvent orderOutboxEvent = new OrderOutboxEvent();
        orderOutboxEvent.setId(rs.getLong("id"));
        orderOutboxEvent.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        orderOutboxEvent.setType(OrderOutboxEventType.valueOf(rs.getString("type")));
        orderOutboxEvent.setUpdatedAt(Optional.ofNullable(rs.getTimestamp("updated_at")).map(Timestamp::toInstant).orElse(null));
        orderOutboxEvent.setStatus(OrderOutboxEventStatus.valueOf(rs.getString("status")));
        orderOutboxEvent.setPayload(rs.getString("payload"));
        return orderOutboxEvent;
    };

    public List<Order> findAllOrdersByCustomerId(Long customerId) {
        return jdbcTemplate.query("select * from orders.orders where customer_id = ?", orderRowMapper, customerId);
    }

    public List<OrderItem> findAllOrderItemsByOrderId(Long orderId) {
        return jdbcTemplate.query("select * from orders.order_items where order_id = ?", orderItemsRowMapper, orderId);
    }

    public List<OrderOutboxEvent> findAllOrderOutboxEvents() {
        return jdbcTemplate.query("select * from outbox.outbox_events", orderOutboxEventsRowMapper, null);
    }
}
