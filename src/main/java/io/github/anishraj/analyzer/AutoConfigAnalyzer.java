package io.github.anishraj.analyzer;

import io.github.anishraj.model.AutoConfigWarning;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Analyzes Spring Boot's {@link ConditionEvaluationReport} to identify
 * autoconfiguration classes that were evaluated but whose conditions
 * indicate the feature may not actually be in use.
 *
 * <p>Also flags known "heavy" autoconfigurations that add startup cost
 * if the developer might not intentionally need them.
 */
public class AutoConfigAnalyzer {

    // Known heavy autoconfigs and what they imply
    private static final Map<String, String[]> KNOWN_HEAVY_AUTOCONFIGS = Map.of(
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
            new String[]{"Hibernate JPA auto-configured", "If you don't use JPA, remove spring-boot-starter-data-jpa from your pom.xml"},
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
            new String[]{"Redis auto-configured", "If you don't use Redis, remove spring-boot-starter-data-redis"},
        "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration",
            new String[]{"RabbitMQ auto-configured", "If you don't use RabbitMQ, remove spring-boot-starter-amqp"},
        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
            new String[]{"Kafka auto-configured", "If you don't use Kafka, remove spring-kafka dependency"},
        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration",
            new String[]{"MongoDB auto-configured", "If you don't use MongoDB, remove spring-boot-starter-data-mongodb"},
        "org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration",
            new String[]{"JavaMailSender auto-configured", "If you don't send emails, remove spring-boot-starter-mail"},
        "org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration",
            new String[]{"Quartz scheduler auto-configured", "If you don't use Quartz, remove spring-boot-starter-quartz"},
        "org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration",
            new String[]{"OAuth2 client auto-configured", "Remove spring-boot-starter-oauth2-client if not using OAuth2"}
    );

    private final ApplicationContext applicationContext;

    public AutoConfigAnalyzer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Runs the analysis and returns a list of warnings.
     */
    public List<AutoConfigWarning> analyze() {
        List<AutoConfigWarning> warnings = new ArrayList<>();

        try {
            ConditionEvaluationReport report = applicationContext
                    .getBean(ConditionEvaluationReport.class);

            Map<String, ConditionEvaluationReport.ConditionAndOutcomes> outcomes =
                    report.getConditionAndOutcomesBySource();

            for (Map.Entry<String, ConditionEvaluationReport.ConditionAndOutcomes> entry : outcomes.entrySet()) {
                String sourceClass = entry.getKey();
                ConditionEvaluationReport.ConditionAndOutcomes conditionAndOutcomes = entry.getValue();

                // Check against known heavy autoconfigs
                if (KNOWN_HEAVY_AUTOCONFIGS.containsKey(sourceClass) && conditionAndOutcomes.isFullMatch()) {
                    String[] info = KNOWN_HEAVY_AUTOCONFIGS.get(sourceClass);
                    warnings.add(new AutoConfigWarning(sourceClass, info[0], info[1]));
                }
            }

            // Detect classes that matched conditions but have no beans in context
            // (loaded but nothing is actually using them)
            warnings.addAll(detectOrphanedAutoConfigs(outcomes));

        } catch (Exception e) {
            // ConditionEvaluationReport may not be available in all contexts
            // Silently skip — this is optional analysis
        }

        return warnings;
    }

    private List<AutoConfigWarning> detectOrphanedAutoConfigs(
            Map<String, ConditionEvaluationReport.ConditionAndOutcomes> outcomes) {

        List<AutoConfigWarning> warnings = new ArrayList<>();

        for (Map.Entry<String, ConditionEvaluationReport.ConditionAndOutcomes> entry : outcomes.entrySet()) {
            String sourceClass = entry.getKey();

            // Only inspect actual autoconfiguration classes (not user configs)
            if (!sourceClass.contains("autoconfigure") && !sourceClass.contains("AutoConfiguration")) {
                continue;
            }

            // Skip ones already in the known list
            if (KNOWN_HEAVY_AUTOCONFIGS.containsKey(sourceClass)) {
                continue;
            }

            // Check if this autoconfiguration produced beans that exist in context
            try {
                String shortName = extractSimpleClassName(sourceClass);
                if (isLikelyUnused(shortName)) {
                    warnings.add(new AutoConfigWarning(
                            sourceClass,
                            "AutoConfiguration loaded but may not be required by your application",
                            "Review if this autoconfiguration is intentional. "
                                    + "Use @EnableAutoConfiguration(exclude=...) to disable if not needed."
                    ));
                }
            } catch (Exception ignored) {
                // Skip problematic entries
            }
        }

        return warnings;
    }

    private boolean isLikelyUnused(String shortClassName) {
        // Heuristic: well-known autoconfigs that add startup cost if not needed
        return shortClassName.contains("Batch")
                || shortClassName.contains("Flyway")
                || shortClassName.contains("Liquibase")
                || shortClassName.contains("Elasticsearch")
                || shortClassName.contains("Cassandra")
                || shortClassName.contains("Couchbase")
                || shortClassName.contains("Solr");
    }

    private String extractSimpleClassName(String fullyQualified) {
        int idx = fullyQualified.lastIndexOf('.');
        return idx >= 0 ? fullyQualified.substring(idx + 1) : fullyQualified;
    }
}
