package io.github.anishraj.model;

/**
 * Holds timing and metadata for a single Spring bean's initialization.
 */
public class BeanMetric {

    private final String beanName;
    private final String className;
    private final long initTimeMs;
    private final String category;
    private final boolean slow;

    public BeanMetric(String beanName, String className, long initTimeMs, long slowThresholdMs) {
        this.beanName    = beanName;
        this.className   = className;
        this.initTimeMs  = initTimeMs;
        this.slow        = initTimeMs >= slowThresholdMs;
        this.category    = categorize(className);
    }

    /**
     * Derives a human-readable category from the class name.
     * Used for grouping in the HTML report's pie chart.
     */
    private String categorize(String className) {
        if (className == null) return "Unknown";
        String lower = className.toLowerCase();
        if (lower.contains("controller") || lower.contains("restcontroller")) return "Controller";
        if (lower.contains("service"))    return "Service";
        if (lower.contains("repository") || lower.contains("dao")) return "Repository";
        if (lower.contains("config") || lower.contains("configuration")) return "Configuration";
        if (lower.contains("datasource") || lower.contains("dataSource")) return "DataSource";
        if (lower.contains("security"))   return "Security";
        if (lower.contains("cache"))      return "Cache";
        if (lower.contains("scheduler") || lower.contains("task")) return "Scheduler";
        if (lower.contains("filter") || lower.contains("interceptor")) return "Filter/Interceptor";
        if (lower.contains("autoconfigure") || lower.contains("autoconfig")) return "AutoConfiguration";
        return "Infrastructure";
    }

    public String getBeanName()  { return beanName; }
    public String getClassName() { return className; }
    public long   getInitTimeMs(){ return initTimeMs; }
    public String getCategory()  { return category; }
    public boolean isSlow()      { return slow; }

    @Override
    public String toString() {
        return String.format("BeanMetric{name='%s', time=%dms, slow=%b}", beanName, initTimeMs, slow);
    }
}
