package io.github.anishraj.demo.service;

import io.github.anishraj.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inventory management service — monitors stock levels and triggers restocking alerts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public long getLowStockCount() {
        return productRepository.findByAvailableTrue().stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() < LOW_STOCK_THRESHOLD)
                .count();
    }

    /**
     * Runs every hour to check for low-stock items.
     * Another prime lazy-load candidate — only needs to be active post-startup.
     */
    @Scheduled(fixedRateString = "${inventory.check-interval-ms:3600000}")
    public void checkStockLevels() {
        long lowStock = getLowStockCount();
        if (lowStock > 0) {
            log.warn("Low stock alert: {} products below threshold", lowStock);
            notificationService.sendAdminAlert(lowStock + " products are low on stock!");
        }
    }

    @Transactional
    public void markOutOfStock(Long productId) {
        productRepository.findById(productId).ifPresent(p -> {
            p.setAvailable(false);
            p.setStockQuantity(0);
            productRepository.save(p);
            log.info("Product {} marked out of stock", p.getName());
        });
    }
}
