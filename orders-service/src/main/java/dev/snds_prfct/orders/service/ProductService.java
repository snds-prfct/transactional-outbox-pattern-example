package dev.snds_prfct.orders.service;

import dev.snds_prfct.orders.entity.products.Product;
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

    public List<Product> findProducts(Set<Long> requiredProductIds) {
        List<Product> foundProducts = productRepository.findAllById(requiredProductIds);
        validateAllProductsAreFound(requiredProductIds, foundProducts);
        validateProductsAreAvailable(foundProducts);
        return foundProducts;
    }

    private void validateAllProductsAreFound(Set<Long> requiredProductIds, List<Product> foundProducts) {
        if (foundProducts.size() != requiredProductIds.size()) {
            throw new ProductsNotFoundException();
        }
    }

    private void validateProductsAreAvailable(List<Product> foundProducts) {
        List<Long> notAvailableProducts = foundProducts.stream()
                .filter(product -> !product.getIsAvailable())
                .map(Product::getId)
                .toList();
        if (!notAvailableProducts.isEmpty()) {
            throw new ProductsNotAvailableException(notAvailableProducts);
        }
    }
}
