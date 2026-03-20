package io.github.anishraj.demo.service;

import io.github.anishraj.demo.repository.OrderRepository;
import io.github.anishraj.demo.repository.ProductRepository;
import io.github.anishraj.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Business reporting service — generates summary metrics.
 * Good lazy-load candidate: only needed when reports are requested.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportingService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public Map<String, Long> getDashboardMetrics() {
        Map<String, Long> metrics = new HashMap<>();
        metrics.put("totalUsers",    userRepository.count());
        metrics.put("totalProducts", productRepository.count());
        metrics.put("totalOrders",   orderRepository.count());
        metrics.put("pendingOrders", orderRepository.countByStatus(
                io.github.anishraj.demo.model.Order.OrderStatus.PENDING));
        return metrics;
    }

    /**
     * Scheduled daily report — runs at midnight.
     * Perfect lazy-load candidate: not needed until the first scheduled execution.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void generateDailyReport() {
        log.info("Generating daily business report...");
        Map<String, Long> metrics = getDashboardMetrics();
        log.info("Daily report: users={}, products={}, orders={}",
                metrics.get("totalUsers"),
                metrics.get("totalProducts"),
                metrics.get("totalOrders"));
    }
}
