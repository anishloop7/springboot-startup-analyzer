package io.github.anishraj.report;

import io.github.anishraj.model.BeanMetric;
import io.github.anishraj.model.BlockingCallWarning;
import io.github.anishraj.model.StartupAnalysisReport;

import java.util.List;

/**
 * Prints a formatted startup analysis summary to the console (stdout)
 * immediately after application startup completes.
 *
 * <p>Output example:
 * <pre>
 * ╔══════════════════════════════════════════════════════════════╗
 * ║         🚀 SpringBoot Startup Analyzer - Report             ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  Application : my-service                                   ║
 * ║  Total Startup Time : 3420ms                                ║
 * ║  Total Beans : 147  |  Slow Beans : 5  |  Warnings : 3     ║
 * ╚══════════════════════════════════════════════════════════════╝
 * </pre>
 */
public class ConsoleReportPrinter {

    private static final String RESET  = "\u001B[0m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String CYAN   = "\u001B[36m";
    private static final String BOLD   = "\u001B[1m";

    public void print(StartupAnalysisReport report) {
        System.out.println();
        printHeader(report);
        printSlowBeans(report);
        printBlockingWarnings(report);
        printLazyLoadSuggestions(report);
        printFooter(report);
        System.out.println();
    }

    private void printHeader(StartupAnalysisReport report) {
        String line = "═".repeat(70);
        System.out.println(CYAN + BOLD + "╔" + line + "╗" + RESET);
        System.out.println(CYAN + BOLD + "║" + RESET
                + center("🚀  SpringBoot Startup Analyzer  🛡️", 70)
                + CYAN + BOLD + "║" + RESET);
        System.out.println(CYAN + BOLD + "╠" + line + "╣" + RESET);
        System.out.printf(CYAN + "║" + RESET + "  %-20s : %-45s" + CYAN + "║%n" + RESET,
                "Application", report.getApplicationName());
        System.out.printf(CYAN + "║" + RESET + "  %-20s : %-45s" + CYAN + "║%n" + RESET,
                "Total Startup Time", report.getTotalStartupTimeMs() + "ms");
        System.out.printf(CYAN + "║" + RESET + "  %-20s : %-45s" + CYAN + "║%n" + RESET,
                "Total Beans", report.getTotalBeanCount());
        System.out.printf(CYAN + "║" + RESET + "  %-20s : " + RED + "%-45s" + CYAN + "║%n" + RESET,
                "Slow Beans", report.getSlowBeanCount());
        System.out.printf(CYAN + "║" + RESET + "  %-20s : %-45s" + CYAN + "║%n" + RESET,
                "Warnings", report.getWarningCount());
        System.out.printf(CYAN + "║" + RESET + "  %-20s : %-45s" + CYAN + "║%n" + RESET,
                "Report Generated", report.getFormattedGeneratedAt());
        System.out.println(CYAN + BOLD + "╚" + line + "╝" + RESET);
    }

    private void printSlowBeans(StartupAnalysisReport report) {
        List<BeanMetric> slowBeans = report.getTopSlowBeans(10);
        if (slowBeans.isEmpty()) {
            System.out.println(GREEN + "\n  ✅  No slow beans detected!" + RESET);
            return;
        }

        System.out.println(BOLD + "\n  🐢  TOP SLOW BEANS (sorted by init time)" + RESET);
        System.out.println("  " + "─".repeat(68));
        System.out.printf("  %-5s  %-40s  %-12s  %s%n", "RANK", "BEAN NAME", "TIME (ms)", "CATEGORY");
        System.out.println("  " + "─".repeat(68));

        int rank = 1;
        for (BeanMetric bean : slowBeans) {
            String color = bean.getInitTimeMs() > 500 ? RED
                    : bean.getInitTimeMs() > 200 ? YELLOW
                    : RESET;
            String truncatedName = truncate(bean.getBeanName(), 40);
            System.out.printf("  %-5s  " + color + "%-40s  %-12s" + RESET + "  %s%n",
                    "#" + rank++,
                    truncatedName,
                    bean.getInitTimeMs() + "ms",
                    bean.getCategory());
        }
    }

    private void printBlockingWarnings(StartupAnalysisReport report) {
        if (report.getBlockingCallWarnings().isEmpty()) return;

        System.out.println(BOLD + "\n  ⚠️   BLOCKING CALL WARNINGS" + RESET);
        System.out.println("  " + "─".repeat(68));
        for (BlockingCallWarning w : report.getBlockingCallWarnings()) {
            System.out.printf("  " + YELLOW + "%-18s" + RESET + " %s%n",
                    w.getTypeLabel(), truncate(w.getBeanName(), 48));
            System.out.printf("  %-18s " + CYAN + "%s%n" + RESET,
                    "", truncate(w.getRecommendation(), 60));
        }
    }

    private void printLazyLoadSuggestions(StartupAnalysisReport report) {
        List<String> candidates = report.getLazyLoadCandidates();
        if (candidates.isEmpty()) return;

        System.out.println(BOLD + "\n  💡  LAZY LOAD CANDIDATES" + RESET);
        System.out.println("  " + "─".repeat(68));
        System.out.println("  These beans could be annotated with @Lazy to improve startup time:");
        candidates.stream().limit(8).forEach(name ->
                System.out.println("  " + GREEN + "  → " + RESET + name));
        if (candidates.size() > 8) {
            System.out.println("  " + GREEN + "  ... and " + (candidates.size() - 8) + " more (see HTML report)" + RESET);
        }
    }

    private void printFooter(StartupAnalysisReport report) {
        System.out.println();
        System.out.println(BOLD + "  📄  HTML Report saved to: " + RESET + GREEN + "startup-report.html" + RESET);
        System.out.println("  " + "─".repeat(68));
        System.out.println(CYAN + "  SpringBoot Startup Analyzer by github.com/anishraj  |  v1.0.0" + RESET);
    }

    private String center(String text, int width) {
        int padding = (width - text.length()) / 2;
        String pad = " ".repeat(Math.max(0, padding));
        return pad + text + " ".repeat(Math.max(0, width - pad.length() - text.length()));
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }
}
