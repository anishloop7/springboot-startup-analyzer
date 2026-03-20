package io.github.anishraj.demo.service;

import io.github.anishraj.demo.model.Order;
import io.github.anishraj.demo.model.User;
import io.github.anishraj.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order lifecycle management service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Transactional
    public Order placeOrder(User user, BigDecimal totalAmount) {
        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.PENDING)
                .placedAt(LocalDateTime.now())
                .build();
        Order saved = orderRepository.save(order);
        log.info("Order #{} placed for user {}", saved.getId(), user.getUsername());

        // Async notification — doesn't block the order flow
        notificationService.sendOrderConfirmation(user.getEmail(), saved.getId());
        return saved;
    }

    @Transactional
    public Optional<Order> updateStatus(Long orderId, Order.OrderStatus newStatus) {
        return orderRepository.findById(orderId).map(order -> {
            order.setStatus(newStatus);
            if (newStatus == Order.OrderStatus.DELIVERED) {
                order.setDeliveredAt(LocalDateTime.now());
            }
            return orderRepository.save(order);
        });
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findRecentOrdersByUser(userId);
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(Order.OrderStatus.PENDING);
    }

    public long countPendingOrders() {
        return orderRepository.countByStatus(Order.OrderStatus.PENDING);
    }
}
