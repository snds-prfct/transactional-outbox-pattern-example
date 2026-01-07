package dev.snds_prfct.orders.service;

import dev.snds_prfct.orders.entity.orders.Product;
import dev.snds_prfct.orders.exception.ProductsNotAvailableException;
import dev.snds_prfct.orders.exception.ProductsNotFoundException;
import dev.snds_prfct.orders.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> checkProductsAvailability(Set<Long> requiredProductIds) {
        List<Product> foundProducts = productRepository.findAllById(requiredProductIds);
        if (foundProducts.size() != requiredProductIds.size()) {
            HashSet<Long> notFoundProductIds = new HashSet<>(requiredProductIds);
            foundProducts.stream().map(Product::getId).toList().forEach(notFoundProductIds::remove);
            throw new ProductsNotFoundException(notFoundProductIds);
        }

        List<Product> notAvailableProducts = foundProducts.stream()
                .filter(product -> !product.getIsAvailable())
                .toList();
        if (!notAvailableProducts.isEmpty()) {
            throw new ProductsNotAvailableException(notAvailableProducts.stream().map(Product::getId).toList());
        }
        return foundProducts;
    }
}
