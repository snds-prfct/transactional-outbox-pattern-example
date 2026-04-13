package dev.snds_prfct.orders.repository;

import dev.snds_prfct.orders.constant.OrderStatus;
import dev.snds_prfct.orders.entity.orders.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByCustomerIdAndIdempotencyKey(Long customerId, UUID idempotencyKey);

    Page<Order> findAllOrdersByCustomerId(Long customerId, Pageable pageable);

    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :orderId")
    void updateOrderStatus(Long orderId, OrderStatus status);
}
