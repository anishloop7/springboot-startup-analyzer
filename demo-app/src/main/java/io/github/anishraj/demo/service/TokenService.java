package io.github.anishraj.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT / API token management service.
 * Demonstrates a stateful service with in-memory token store (illustrative).
 */
@Slf4j
@Service
public class TokenService {

    @Value("${app.token.expiry-seconds:3600}")
    private long expirySeconds;

    private final Map<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateApiToken(String userId) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokenStore.put(token, new TokenEntry(userId, System.currentTimeMillis() + expirySeconds * 1000));
        log.debug("Generated API token for user {}", userId);
        return token;
    }

    public boolean validateToken(String token) {
        TokenEntry entry = tokenStore.get(token);
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry.expiresAt()) {
            tokenStore.remove(token);
            return false;
        }
        return true;
    }

    public void revokeToken(String token) {
        tokenStore.remove(token);
    }

    public String generatePasswordResetToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public long getActiveTokenCount() {
        long now = System.currentTimeMillis();
        tokenStore.entrySet().removeIf(e -> e.getValue().expiresAt() < now);
        return tokenStore.size();
    }

    private record TokenEntry(String userId, long expiresAt) {}
}
