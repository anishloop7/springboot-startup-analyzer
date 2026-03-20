package io.github.anishraj.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Demo application for SpringBoot Startup Analyzer.
 *
 * <p>Run this app and the analyzer will automatically:
 * <ul>
 *   <li>Time every bean's initialization</li>
 *   <li>Print a formatted summary table to console</li>
 *   <li>Generate startup-report.html in the project root</li>
 * </ul>
 *
 * No annotations, no config — just run it!
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
