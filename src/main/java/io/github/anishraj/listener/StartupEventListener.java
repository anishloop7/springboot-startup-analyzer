package io.github.anishraj.listener;

import io.github.anishraj.analyzer.AutoConfigAnalyzer;
import io.github.anishraj.analyzer.BlockingCallDetector;
import io.github.anishraj.analyzer.LazyLoadSuggester;
import io.github.anishraj.config.AnalyzerProperties;
import io.github.anishraj.model.AutoConfigWarning;
import io.github.anishraj.model.BeanMetric;
import io.github.anishraj.model.BlockingCallWarning;
import io.github.anishraj.model.StartupAnalysisReport;
import io.github.anishraj.processor.BeanTimingPostProcessor;
import io.github.anishraj.report.ConsoleReportPrinter;
import io.github.anishraj.report.HtmlReportGenerator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Listens for {@link ApplicationReadyEvent} — the signal that the Spring context
 * is fully initialized and the application is ready to serve requests.
 *
 * <p>At this point all bean metrics have been collected, so we run the analyzers,
 * build the {@link StartupAnalysisReport}, and dispatch to both the console printer
 * and the HTML report generator.
 */
public class StartupEventListener {

    private final BeanTimingPostProcessor timingProcessor;
    private final AutoConfigAnalyzer      autoConfigAnalyzer;
    private final BlockingCallDetector    blockingCallDetector;
    private final LazyLoadSuggester       lazyLoadSuggester;
    private final HtmlReportGenerator     htmlReportGenerator;
    private final ConsoleReportPrinter    consoleReportPrinter;
    private final AnalyzerProperties      properties;
    private final ApplicationContext      applicationContext;

    public StartupEventListener(
            BeanTimingPostProcessor timingProcessor,
            AutoConfigAnalyzer autoConfigAnalyzer,
            BlockingCallDetector blockingCallDetector,
            LazyLoadSuggester lazyLoadSuggester,
            HtmlReportGenerator htmlReportGenerator,
            ConsoleReportPrinter consoleReportPrinter,
            AnalyzerProperties properties,
            ApplicationContext applicationContext) {

        this.timingProcessor      = timingProcessor;
        this.autoConfigAnalyzer   = autoConfigAnalyzer;
        this.blockingCallDetector = blockingCallDetector;
        this.lazyLoadSuggester    = lazyLoadSuggester;
        this.htmlReportGenerator  = htmlReportGenerator;
        this.consoleReportPrinter = consoleReportPrinter;
        this.properties           = properties;
        this.applicationContext   = applicationContext;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        if (!properties.isEnabled()) return;

        try {
            long jvmUptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
            String appName   = resolveApplicationName(event);

            // ── Step 1: Collect bean metrics ──────────────────────────────
            List<BeanMetric> beanMetrics = timingProcessor.getCollectedMetrics();

            // ── Step 2: Build the report ──────────────────────────────────
            StartupAnalysisReport report = new StartupAnalysisReport(appName, jvmUptimeMs);
            beanMetrics.forEach(report::addBeanMetric);

            // ── Step 3: Run analyzers ─────────────────────────────────────
            if (properties.isAutoConfigAnalysisEnabled()) {
                List<AutoConfigWarning> autoWarnings = autoConfigAnalyzer.analyze();
                autoWarnings.forEach(report::addAutoConfigWarning);
            }

            if (properties.isBlockingCallDetectionEnabled()) {
                List<BlockingCallWarning> blockingWarnings = blockingCallDetector.detect(beanMetrics);
                blockingWarnings.forEach(report::addBlockingCallWarning);
            }

            if (properties.isLazyLoadSuggestionsEnabled()) {
                List<String> lazyCandidates = lazyLoadSuggester.suggest(beanMetrics);
                lazyCandidates.forEach(report::addLazyLoadCandidate);
            }

            // ── Step 4: Output ────────────────────────────────────────────
            if (properties.isConsoleReportEnabled()) {
                consoleReportPrinter.print(report);
            }

            if (properties.isHtmlReportEnabled()) {
                htmlReportGenerator.generate(report, properties.getHtmlReportPath());
            }

        } catch (Exception e) {
            System.err.println("[SpringBoot Startup Analyzer] Failed to generate report: " + e.getMessage());
        }
    }

    private String resolveApplicationName(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String name = env.getProperty("spring.application.name");
        return (name != null && !name.isBlank()) ? name : "spring-boot-app";
    }
}
