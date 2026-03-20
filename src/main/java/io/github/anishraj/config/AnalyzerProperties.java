package io.github.anishraj.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Optional configuration for SpringBoot Startup Analyzer.
 * All properties have sensible defaults — zero config required.
 *
 * <p>Override in application.properties with prefix {@code startup.analyzer}:
 * <pre>
 *   startup.analyzer.enabled=true
 *   startup.analyzer.slow-bean-threshold-ms=100
 *   startup.analyzer.html-report-enabled=true
 *   startup.analyzer.html-report-path=startup-report.html
 *   startup.analyzer.console-report-enabled=true
 * </pre>
 */
@ConfigurationProperties(prefix = "startup.analyzer")
public class AnalyzerProperties {

    /** Master switch to enable/disable the analyzer entirely. Default: true */
    private boolean enabled = true;

    /** Beans taking longer than this (ms) are flagged as slow. Default: 100ms */
    private long slowBeanThresholdMs = 100;

    /** Whether to generate the HTML report file. Default: true */
    private boolean htmlReportEnabled = true;

    /** Output path for the HTML report. Default: startup-report.html (project root) */
    private String htmlReportPath = "startup-report.html";

    /** Whether to print the summary table to console. Default: true */
    private boolean consoleReportEnabled = true;

    /** Whether to detect and warn about blocking calls. Default: true */
    private boolean blockingCallDetectionEnabled = true;

    /** Whether to suggest lazy-loadable beans. Default: true */
    private boolean lazyLoadSuggestionsEnabled = true;

    /** Whether to analyze unnecessary autoconfiguration. Default: true */
    private boolean autoConfigAnalysisEnabled = true;

    /** Minimum bean init time (ms) to include in report. Default: 0 (show all) */
    private long minimumBeanTimeMs = 0;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getSlowBeanThresholdMs() { return slowBeanThresholdMs; }
    public void setSlowBeanThresholdMs(long slowBeanThresholdMs) { this.slowBeanThresholdMs = slowBeanThresholdMs; }

    public boolean isHtmlReportEnabled() { return htmlReportEnabled; }
    public void setHtmlReportEnabled(boolean htmlReportEnabled) { this.htmlReportEnabled = htmlReportEnabled; }

    public String getHtmlReportPath() { return htmlReportPath; }
    public void setHtmlReportPath(String htmlReportPath) { this.htmlReportPath = htmlReportPath; }

    public boolean isConsoleReportEnabled() { return consoleReportEnabled; }
    public void setConsoleReportEnabled(boolean consoleReportEnabled) { this.consoleReportEnabled = consoleReportEnabled; }

    public boolean isBlockingCallDetectionEnabled() { return blockingCallDetectionEnabled; }
    public void setBlockingCallDetectionEnabled(boolean v) { this.blockingCallDetectionEnabled = v; }

    public boolean isLazyLoadSuggestionsEnabled() { return lazyLoadSuggestionsEnabled; }
    public void setLazyLoadSuggestionsEnabled(boolean v) { this.lazyLoadSuggestionsEnabled = v; }

    public boolean isAutoConfigAnalysisEnabled() { return autoConfigAnalysisEnabled; }
    public void setAutoConfigAnalysisEnabled(boolean v) { this.autoConfigAnalysisEnabled = v; }

    public long getMinimumBeanTimeMs() { return minimumBeanTimeMs; }
    public void setMinimumBeanTimeMs(long minimumBeanTimeMs) { this.minimumBeanTimeMs = minimumBeanTimeMs; }
}
