package io.github.anishraj.analyzer;

import io.github.anishraj.model.BeanMetric;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Identifies Spring beans that are good candidates for lazy initialization.
 *
 * <p>A bean is a good lazy-load candidate if:
 * <ul>
 *   <li>It is not in the core startup path (no other bean depends on it directly)</li>
 *   <li>Its class name suggests it is a peripheral feature (admin, reporting, scheduled, etc.)</li>
 *   <li>It has a non-trivial initialization time but is unlikely to be called immediately</li>
 * </ul>
 *
 * <p>Enabling lazy init globally:
 * <pre>spring.main.lazy-initialization=true</pre>
 * Or selectively with {@code @Lazy} on the bean definition.
 */
public class LazyLoadSuggester {

    // Patterns that suggest a bean is non-critical at startup
    private static final Set<String> LAZY_CANDIDATE_PATTERNS = Set.of(
            "admin", "management", "report", "reporting",
            "scheduler", "scheduled", "batch", "job",
            "export", "import", "migration", "audit",
            "notification", "email", "mail", "sms",
            "metric", "monitor", "health",
            "swagger", "springdoc", "openapi",
            "devtools", "actuator",
            "async", "executor", "thread"
    );

    // Patterns that indicate a bean is critical — never suggest lazy for these
    private static final Set<String> CRITICAL_BEAN_PATTERNS = Set.of(
            "security", "datasource", "transactionmanager",
            "entitymanager", "sessionfactory", "authenticationmanager",
            "passwordencoder", "tokenstore", "jwtfilter",
            "errorcontroller", "dispatcherservlet"
    );

    private final org.springframework.context.ConfigurableApplicationContext applicationContext;

    public LazyLoadSuggester(org.springframework.context.ApplicationContext applicationContext) {
        this.applicationContext = (org.springframework.context.ConfigurableApplicationContext) applicationContext;
    }
    /**
     * Evaluates collected bean metrics and returns bean names that are
     * good candidates for {@code @Lazy} initialization.
     */
    public List<String> suggest(List<BeanMetric> beanMetrics) {
        List<String> candidates = new ArrayList<>();

        String[] allBeanNames = applicationContext.getBeanDefinitionNames();

        for (BeanMetric metric : beanMetrics) {
            String name  = metric.getBeanName().toLowerCase();
            String clazz = metric.getClassName() != null
                    ? metric.getClassName().toLowerCase() : "";

            // Never suggest lazy for critical infrastructure beans
            if (matchesAny(name, CRITICAL_BEAN_PATTERNS)
                    || matchesAny(clazz, CRITICAL_BEAN_PATTERNS)) {
                continue;
            }

            // Suggest if name/class matches peripheral patterns
            if (matchesAny(name, LAZY_CANDIDATE_PATTERNS)
                    || matchesAny(clazz, LAZY_CANDIDATE_PATTERNS)) {

                // Confirm the bean is not heavily depended upon
                if (!isHeavilyDependedUpon(metric.getBeanName(), allBeanNames)) {
                    candidates.add(metric.getBeanName());
                }
            }
        }

        return candidates;
    }

    /**
     * Checks if other beans declared a dependency on this bean.
     * If many beans depend on it, laziness won't help startup time much.
     * ConfigurableApplicationContext
     */
    private boolean isHeavilyDependedUpon(String beanName, String[] allBeanNames) {
        int dependentCount = 0;
        for (String name : allBeanNames) {
            try {
                String[] deps = applicationContext.getBeanFactory().getDependenciesForBean(name);
                for (String dep : deps) {
                    if (dep.equals(beanName)) {
                        dependentCount++;
                        if (dependentCount >= 3) return true;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private boolean matchesAny(String target, Set<String> patterns) {
        for (String pattern : patterns) {
            if (target.contains(pattern)) return true;
        }
        return false;
    }
}
