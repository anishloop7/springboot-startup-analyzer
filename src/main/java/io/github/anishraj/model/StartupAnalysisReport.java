package io.github.anishraj.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Central aggregation of all startup analysis data.
 * Passed to both {@link io.github.anishraj.report.HtmlReportGenerator}
 * and {@link io.github.anishraj.report.ConsoleReportPrinter}.
 */
public class StartupAnalysisReport {

    private final String applicationName;
    private final LocalDateTime generatedAt;
    private final long totalStartupTimeMs;

    private final List<BeanMetric>          beanMetrics         = new ArrayList<>();
    private final List<AutoConfigWarning>   autoConfigWarnings  = new ArrayList<>();
    private final List<BlockingCallWarning> blockingCallWarnings = new ArrayList<>();
    private final List<String>              lazyLoadCandidates  = new ArrayList<>();

    public StartupAnalysisReport(String applicationName, long totalStartupTimeMs) {
        this.applicationName  = applicationName;
        this.totalStartupTimeMs = totalStartupTimeMs;
        this.generatedAt      = LocalDateTime.now();
    }

    // ─── Mutators ─────────────────────────────────────────────────────────────

    public void addBeanMetric(BeanMetric metric)               { beanMetrics.add(metric); }
    public void addAutoConfigWarning(AutoConfigWarning w)      { autoConfigWarnings.add(w); }
    public void addBlockingCallWarning(BlockingCallWarning w)  { blockingCallWarnings.add(w); }
    public void addLazyLoadCandidate(String beanName)          { lazyLoadCandidates.add(beanName); }

    // ─── Derived Statistics ───────────────────────────────────────────────────

    public int getTotalBeanCount()      { return beanMetrics.size(); }
    public int getSlowBeanCount()       { return (int) beanMetrics.stream().filter(BeanMetric::isSlow).count(); }
    public int getWarningCount()        { return autoConfigWarnings.size() + blockingCallWarnings.size(); }

    public long getTotalBeanInitTimeMs() {
        return beanMetrics.stream().mapToLong(BeanMetric::getInitTimeMs).sum();
    }

    public double getAverageBeanInitTimeMs() {
        return beanMetrics.isEmpty() ? 0 :
                beanMetrics.stream().mapToLong(BeanMetric::getInitTimeMs).average().orElse(0);
    }

    /** Top N slowest beans, sorted descending by init time. */
    public List<BeanMetric> getTopSlowBeans(int n) {
        return beanMetrics.stream()
                .sorted(Comparator.comparingLong(BeanMetric::getInitTimeMs).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    /** All beans sorted by init time descending. */
    public List<BeanMetric> getBeanMetricsSorted() {
        return beanMetrics.stream()
                .sorted(Comparator.comparingLong(BeanMetric::getInitTimeMs).reversed())
                .collect(Collectors.toList());
    }

    /** Bean count grouped by category — for the pie chart. */
    public Map<String, Long> getBeansByCategory() {
        return beanMetrics.stream()
                .collect(Collectors.groupingBy(BeanMetric::getCategory, Collectors.counting()));
    }

    /** Init time (ms) grouped by category — for stacked charts. */
    public Map<String, Long> getTimeByCategory() {
        return beanMetrics.stream()
                .collect(Collectors.groupingBy(BeanMetric::getCategory,
                        Collectors.summingLong(BeanMetric::getInitTimeMs)));
    }

    public String getFormattedGeneratedAt() {
        return generatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public String              getApplicationName()      { return applicationName; }
    public LocalDateTime       getGeneratedAt()          { return generatedAt; }
    public long                getTotalStartupTimeMs()   { return totalStartupTimeMs; }
    public List<BeanMetric>    getBeanMetrics()          { return beanMetrics; }
    public List<AutoConfigWarning>   getAutoConfigWarnings()  { return autoConfigWarnings; }
    public List<BlockingCallWarning> getBlockingCallWarnings(){ return blockingCallWarnings; }
    public List<String>        getLazyLoadCandidates()   { return lazyLoadCandidates; }
}
