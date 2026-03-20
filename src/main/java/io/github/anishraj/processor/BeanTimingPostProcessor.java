package io.github.anishraj.processor;

import io.github.anishraj.config.AnalyzerProperties;
import io.github.anishraj.model.BeanMetric;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.PriorityOrdered;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The heart of the analyzer — intercepts every bean's initialization lifecycle
 * using Spring's {@link BeanPostProcessor} hooks.
 *
 * <p>Implements {@link PriorityOrdered} with {@code HIGHEST_PRECEDENCE} so it
 * wraps <em>all</em> other beans, including infrastructure beans.
 *
 * <p>Timeline per bean:
 * <pre>
 *   [postProcessBeforeInitialization] → record start time
 *       bean's @PostConstruct / afterPropertiesSet / init-method runs
 *   [postProcessAfterInitialization]  → record end time → store BeanMetric
 * </pre>
 */
public class BeanTimingPostProcessor implements BeanPostProcessor, PriorityOrdered {

    private final AnalyzerProperties properties;
    private final Map<String, Long>   startTimes   = new ConcurrentHashMap<>();
    private final List<BeanMetric>    beanMetrics  = new ArrayList<>();

    // Beans the analyzer itself registers — exclude to avoid skewed results
    private static final List<String> SELF_BEANS = List.of(
            "startupAnalyzerAutoConfiguration",
            "beanTimingPostProcessor",
            "startupEventListener",
            "autoConfigAnalyzer",
            "blockingCallDetector",
            "lazyLoadSuggester",
            "htmlReportGenerator",
            "consoleReportPrinter",
            "analyzerProperties"
    );

    public BeanTimingPostProcessor(AnalyzerProperties properties) {
        this.properties = properties;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (!SELF_BEANS.contains(beanName)) {
            startTimes.put(beanName, System.currentTimeMillis());
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Long startTime = startTimes.remove(beanName);
        if (startTime != null) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= properties.getMinimumBeanTimeMs()) {
                BeanMetric metric = new BeanMetric(
                        beanName,
                        bean.getClass().getName(),
                        elapsed,
                        properties.getSlowBeanThresholdMs()
                );
                synchronized (beanMetrics) {
                    beanMetrics.add(metric);
                }
            }
        }
        return bean;
    }

    /**
     * Returns a snapshot of all collected bean metrics.
     * Called by {@link io.github.anishraj.listener.StartupEventListener} after startup.
     */
    public List<BeanMetric> getCollectedMetrics() {
        synchronized (beanMetrics) {
            return new ArrayList<>(beanMetrics);
        }
    }
}
