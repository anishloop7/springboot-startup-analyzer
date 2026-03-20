package io.github.anishraj.demo.service;

import io.github.anishraj.demo.model.User;
import io.github.anishraj.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Core user management service.
 * Demonstrates realistic service-layer patterns: caching, transactions, validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String username, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(User.Role.USER)
                .active(true)
                .build();
        User saved = userRepository.save(user);
        log.info("Registered new user: {}", username);
        return saved;
    }

    @Cacheable("users")
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Cacheable("activeUsers")
    public List<User> findAllActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    @CacheEvict(value = {"users", "activeUsers"}, allEntries = true)
    @Transactional
    public void deactivateUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setActive(false);
            userRepository.save(user);
            log.info("Deactivated user: {}", user.getUsername());
        });
    }

    public List<User> findAdmins() {
        return userRepository.findActiveUsersByRole(User.Role.ADMIN);
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }
}
