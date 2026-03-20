package io.github.anishraj.analyzer;

import io.github.anishraj.model.BeanMetric;
import io.github.anishraj.model.BlockingCallWarning;
import io.github.anishraj.model.BlockingCallWarning.Type;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Detects blocking calls that occurred during startup using two strategies:
 *
 * <ol>
 *   <li><b>Class-name heuristics</b> — beans whose class names suggest they
 *       perform I/O (DataSource, RestTemplate, WebClient, FTP, S3, etc.)</li>
 *   <li><b>Timing anomalies</b> — beans with unusually high init time that
 *       are statistically likely to contain blocking operations</li>
 * </ol>
 */
public class BlockingCallDetector {

    // Class name fragments that strongly suggest DB connection at init time
    private static final Set<String> DB_INDICATORS = Set.of(
            "datasource", "connectionpool", "hikari", "c3p0", "dbcp",
            "entitymanagerfactory", "sessionfactory", "jdbctemplate",
            "liquibase", "flyway", "databasepopulator"
    );

    // Class name fragments suggesting outbound HTTP at init time
    private static final Set<String> HTTP_INDICATORS = Set.of(
            "resttemplate", "webclient", "feign", "feignclient",
            "httpclient", "okhttp", "apachehttpclient",
            "openfeign", "retrofit"
    );

    // Class name fragments suggesting file I/O at init time
    private static final Set<String> FILE_IO_INDICATORS = Set.of(
            "resourceloader", "classpathresource", "filereader",
            "propertiesloader", "yamlpropertiesfactorybean",
            "s3client", "blobstorage", "storageservice"
    );

    // Threshold: beans slower than this AND containing certain patterns = likely blocking
    private static final long BLOCKING_TIME_THRESHOLD_MS = 200;

    private final ApplicationContext applicationContext;

    public BlockingCallDetector(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Analyzes the collected bean metrics to detect blocking startup patterns.
     *
     * @param beanMetrics all collected bean metrics from {@link io.github.anishraj.processor.BeanTimingPostProcessor}
     * @return list of detected blocking call warnings
     */
    public List<BlockingCallWarning> detect(List<BeanMetric> beanMetrics) {
        List<BlockingCallWarning> warnings = new ArrayList<>();

        for (BeanMetric metric : beanMetrics) {
            String className = metric.getClassName() != null
                    ? metric.getClassName().toLowerCase() : "";

            // ── Database connection detection ──────────────────────────────
            if (matchesAny(className, DB_INDICATORS)) {
                warnings.add(new BlockingCallWarning(
                        metric.getBeanName(),
                        metric.getClassName(),
                        Type.DATABASE_CONNECTION,
                        "Bean appears to establish a database connection at startup",
                        "Consider using connection pool lazy initialization. "
                                + "Add 'spring.datasource.hikari.initialization-fail-timeout=0' "
                                + "or mark DataSource bean as @Lazy.",
                        metric.getInitTimeMs()
                ));
                continue;
            }

            // ── HTTP call detection ────────────────────────────────────────
            if (matchesAny(className, HTTP_INDICATORS)) {
                warnings.add(new BlockingCallWarning(
                        metric.getBeanName(),
                        metric.getClassName(),
                        Type.HTTP_CALL,
                        "Bean may perform HTTP calls during initialization",
                        "Defer HTTP calls to @PostConstruct methods or use "
                                + "@Async initialization. Consider Circuit Breaker pattern "
                                + "for external service calls at startup.",
                        metric.getInitTimeMs()
                ));
                continue;
            }

            // ── File I/O detection ─────────────────────────────────────────
            if (matchesAny(className, FILE_IO_INDICATORS)) {
                warnings.add(new BlockingCallWarning(
                        metric.getBeanName(),
                        metric.getClassName(),
                        Type.FILE_IO,
                        "Bean appears to perform file or remote storage I/O during initialization",
                        "Cache file reads at the application level. "
                                + "Consider using @RefreshScope with externalized config "
                                + "instead of reading files at startup.",
                        metric.getInitTimeMs()
                ));
                continue;
            }

            // ── Timing anomaly detection ───────────────────────────────────
            if (metric.getInitTimeMs() > BLOCKING_TIME_THRESHOLD_MS && metric.isSlow()) {
                warnings.add(new BlockingCallWarning(
                        metric.getBeanName(),
                        metric.getClassName(),
                        Type.SLOW_INITIALIZATION,
                        String.format("Bean took %dms to initialize — likely contains blocking I/O or heavy computation",
                                metric.getInitTimeMs()),
                        "Profile this bean's constructor and @PostConstruct method. "
                                + "Move heavy work to an async post-startup hook using "
                                + "ApplicationReadyEvent or @Scheduled(initialDelay=...).",
                        metric.getInitTimeMs()
                ));
            }
        }

        return warnings;
    }

    private boolean matchesAny(String className, Set<String> indicators) {
        for (String indicator : indicators) {
            if (className.contains(indicator)) return true;
        }
        return false;
    }
}
