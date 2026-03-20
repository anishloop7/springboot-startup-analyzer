package io.github.anishraj.demo.config;

import io.github.anishraj.demo.model.Product;
import io.github.anishraj.demo.repository.ProductRepository;
import io.github.anishraj.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds the H2 in-memory database with sample data so the demo
 * produces a realistic report with actual bean timings.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserService userService;
    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Seeding demo data...");

        // Seed users
        try {
            userService.register("admin", "admin@demo.com", "Admin@123");
            userService.register("alice", "alice@demo.com", "Alice@123");
            userService.register("bob",   "bob@demo.com",   "Bob@123");
        } catch (IllegalArgumentException e) {
            log.debug("Demo users already exist, skipping seed");
        }

        // Seed products
        if (productRepository.count() == 0) {
            productRepository.save(Product.builder().name("Spring Boot in Action").category("Books")
                    .price(new BigDecimal("49.99")).stockQuantity(100).available(true).build());
            productRepository.save(Product.builder().name("Mechanical Keyboard").category("Electronics")
                    .price(new BigDecimal("129.99")).stockQuantity(25).available(true).build());
            productRepository.save(Product.builder().name("Standing Desk").category("Furniture")
                    .price(new BigDecimal("599.00")).stockQuantity(8).available(true).build());
            productRepository.save(Product.builder().name("USB-C Hub").category("Electronics")
                    .price(new BigDecimal("39.99")).stockQuantity(200).available(true).build());
            productRepository.save(Product.builder().name("Developer Sticker Pack").category("Accessories")
                    .price(new BigDecimal("9.99")).stockQuantity(500).available(true).build());
        }

        log.info("Demo data seeded successfully");
    }
}
