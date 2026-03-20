package io.github.anishraj.demo.repository;

import io.github.anishraj.demo.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);
    List<Product> findByAvailableTrue();
    Page<Product> findByPriceBetween(BigDecimal min, BigDecimal max, Pageable pageable);
    List<Product> findByNameContainingIgnoreCase(String keyword);
    long countByCategory(String category);
}
