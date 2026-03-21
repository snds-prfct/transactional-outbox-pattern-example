package dev.snds_prfct.orders.repository;

import dev.snds_prfct.orders.entity.products.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
