package io.github.anishraj.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Audit trail service — records user actions for compliance.
 * Demonstrates a service with in-memory audit log (illustrative).
 */
@Slf4j
@Service
public class AuditService {

    private final List<AuditEntry> auditLog = new ArrayList<>();

    public void record(String actor, String action, String resource) {
        AuditEntry entry = new AuditEntry(actor, action, resource, LocalDateTime.now());
        auditLog.add(entry);
        log.debug("AUDIT: {} performed {} on {}", actor, action, resource);
    }

    public List<AuditEntry> getAuditLog() {
        return new ArrayList<>(auditLog);
    }

    public long getAuditCount() {
        return auditLog.size();
    }

    public record AuditEntry(String actor, String action, String resource, LocalDateTime timestamp) {}
}
