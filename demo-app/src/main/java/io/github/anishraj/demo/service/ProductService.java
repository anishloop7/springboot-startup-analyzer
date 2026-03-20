package io.github.anishraj.demo.service;

import io.github.anishraj.demo.model.Product;
import io.github.anishraj.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Product catalogue management service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Cacheable("products")
    public List<Product> findAvailableProducts() {
        log.debug("Fetching all available products from DB");
        return productRepository.findByAvailableTrue();
    }

    @Cacheable(value = "productsByCategory", key = "#category")
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public Page<Product> findByPriceRange(BigDecimal min, BigDecimal max, int page, int size) {
        return productRepository.findByPriceBetween(min, max, PageRequest.of(page, size));
    }

    public List<Product> search(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Transactional
    public Product createProduct(Product product) {
        Product saved = productRepository.save(product);
        log.info("Created product: {} (id={})", saved.getName(), saved.getId());
        return saved;
    }

    @Transactional
    public Optional<Product> updateStock(Long productId, int quantity) {
        return productRepository.findById(productId).map(p -> {
            p.setStockQuantity(quantity);
            p.setAvailable(quantity > 0);
            return productRepository.save(p);
        });
    }

    public long countByCategory(String category) {
        return productRepository.countByCategory(category);
    }
}
