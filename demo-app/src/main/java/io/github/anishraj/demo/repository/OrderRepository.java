package io.github.anishraj.demo.repository;

import io.github.anishraj.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.placedAt DESC")
    List<Order> findRecentOrdersByUser(Long userId);

    long countByStatus(Order.OrderStatus status);
}
