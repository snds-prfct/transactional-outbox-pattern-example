INSERT INTO orders.orders (id, customer_id, status, idempotency_key, delivery_address) VALUES
    (1, 1, 'PROCESSING', '117e4dd0-cb10-4c1e-9d5d-4f51a9c85bf0', 'address');
INSERT INTO orders.order_items (id, order_id, product_id, quantity, unit_price) VALUES
    (1, 1, 1, 2, 100);