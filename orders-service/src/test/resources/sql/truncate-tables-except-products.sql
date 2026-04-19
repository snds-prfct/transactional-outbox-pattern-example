
TRUNCATE TABLE orders.order_items, orders.orders, outbox.outbox_events RESTART IDENTITY CASCADE;
