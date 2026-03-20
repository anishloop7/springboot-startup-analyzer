package io.github.anishraj;

import io.github.anishraj.analyzer.AutoConfigAnalyzer;
import io.github.anishraj.analyzer.BlockingCallDetector;
import io.github.anishraj.analyzer.LazyLoadSuggester;
import io.github.anishraj.config.AnalyzerProperties;
import io.github.anishraj.listener.StartupEventListener;
import io.github.anishraj.processor.BeanTimingPostProcessor;
import io.github.anishraj.report.ConsoleReportPrinter;
import io.github.anishraj.report.HtmlReportGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * The autoconfiguration entry point for SpringBoot Startup Analyzer.
 *
 * <p>This class is registered in
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * so Spring Boot picks it up automatically — zero user configuration needed.
 *
 * <p>All beans are conditional on {@code startup.analyzer.enabled=true} (the default),
 * so users can opt out entirely with one property.
 *
 * <p>Each bean is also guarded by {@link ConditionalOnMissingBean} so that
 * advanced users can override any component by defining their own bean.
 */
@AutoConfiguration
@EnableConfigurationProperties(AnalyzerProperties.class)
@ConditionalOnProperty(prefix = "startup.analyzer", name = "enabled", matchIfMissing = true)
public class StartupAnalyzerAutoConfiguration {

    /**
     * The core timing interceptor. Registered as a {@code BeanPostProcessor}
     * so Spring calls it for every bean's initialization lifecycle.
     *
     * <p>Must be a static bean to be registered before the application context
     * fully initializes (a BeanPostProcessor requirement).
     */
    @Bean
    @ConditionalOnMissingBean
    public static BeanTimingPostProcessor beanTimingPostProcessor(AnalyzerProperties properties) {
        return new BeanTimingPostProcessor(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutoConfigAnalyzer autoConfigAnalyzer(ApplicationContext applicationContext) {
        return new AutoConfigAnalyzer(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public BlockingCallDetector blockingCallDetector(ApplicationContext applicationContext) {
        return new BlockingCallDetector(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public LazyLoadSuggester lazyLoadSuggester(ApplicationContext applicationContext) {
        return new LazyLoadSuggester(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public HtmlReportGenerator htmlReportGenerator() {
        return new HtmlReportGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsoleReportPrinter consoleReportPrinter() {
        return new ConsoleReportPrinter();
    }

    /**
     * The orchestrator that ties everything together on {@code ApplicationReadyEvent}.
     */
    @Bean
    @ConditionalOnMissingBean
    public StartupEventListener startupEventListener(
            BeanTimingPostProcessor timingPostProcessor,
            AutoConfigAnalyzer autoConfigAnalyzer,
            BlockingCallDetector blockingCallDetector,
            LazyLoadSuggester lazyLoadSuggester,
            HtmlReportGenerator htmlReportGenerator,
            ConsoleReportPrinter consoleReportPrinter,
            AnalyzerProperties properties,
            ApplicationContext applicationContext) {

        return new StartupEventListener(
                timingPostProcessor,
                autoConfigAnalyzer,
                blockingCallDetector,
                lazyLoadSuggester,
                htmlReportGenerator,
                consoleReportPrinter,
                properties,
                applicationContext
        );
    }
}
