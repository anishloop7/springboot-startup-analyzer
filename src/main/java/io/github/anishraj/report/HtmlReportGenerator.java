package io.github.anishraj.report;

import io.github.anishraj.model.AutoConfigWarning;
import io.github.anishraj.model.BeanMetric;
import io.github.anishraj.model.BlockingCallWarning;
import io.github.anishraj.model.StartupAnalysisReport;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates a beautiful, self-contained HTML dashboard with Chart.js charts
 * showing startup analysis results. Output is a single HTML file with all
 * CSS and JavaScript inline — no external dependencies needed to view it.
 */
public class HtmlReportGenerator {

    public void generate(StartupAnalysisReport report, String outputPath) {
        try {
            Path path = Path.of(outputPath);
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
                writer.println(buildHtml(report));
            }
            System.out.println("\n  ✅  Startup analysis report saved → " + path.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("  ❌  Failed to write HTML report: " + e.getMessage());
        }
    }

    private String buildHtml(StartupAnalysisReport report) {
        List<BeanMetric> topBeans = report.getTopSlowBeans(15);
        Map<String, Long> byCategory = report.getBeansByCategory();

        // Build JSON arrays for Chart.js
        String barLabels = topBeans.stream()
                .map(b -> "\"" + escapeJs(truncate(b.getBeanName(), 30)) + "\"")
                .collect(Collectors.joining(","));
        String barData = topBeans.stream()
                .map(b -> String.valueOf(b.getInitTimeMs()))
                .collect(Collectors.joining(","));
        String barColors = topBeans.stream()
                .map(b -> b.getInitTimeMs() > 500 ? "\"#ef4444\""
                        : b.getInitTimeMs() > 200 ? "\"#f59e0b\""
                        : "\"#6366f1\"")
                .collect(Collectors.joining(","));

        String pieLabels = byCategory.keySet().stream()
                .map(k -> "\"" + escapeJs(k) + "\"")
                .collect(Collectors.joining(","));
        String pieData = byCategory.values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Startup Analysis – %s</title>
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
  <style>
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Segoe UI', system-ui, sans-serif; background: #0f172a; color: #e2e8f0; min-height: 100vh; }
    .header { background: linear-gradient(135deg, #1e1b4b 0%%, #312e81 50%%, #1e1b4b 100%%);
              padding: 2rem 2.5rem; border-bottom: 1px solid #4338ca; }
    .header h1 { font-size: 1.8rem; font-weight: 700; color: #a5b4fc; display: flex; align-items: center; gap: .6rem; }
    .header p  { color: #94a3b8; margin-top: .35rem; font-size: .9rem; }
    .container { max-width: 1400px; margin: 0 auto; padding: 2rem 1.5rem; }
    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; margin-bottom: 2rem; }
    .stat-card  { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.25rem;
                  text-align: center; transition: transform .2s; }
    .stat-card:hover { transform: translateY(-2px); }
    .stat-card .value { font-size: 2.2rem; font-weight: 800; }
    .stat-card .label { font-size: .78rem; color: #94a3b8; margin-top: .3rem; text-transform: uppercase; letter-spacing: .05em; }
    .v-blue   { color: #818cf8; }
    .v-red    { color: #f87171; }
    .v-yellow { color: #fbbf24; }
    .v-green  { color: #34d399; }
    .v-purple { color: #c084fc; }
    .charts-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 1.5rem; margin-bottom: 2rem; }
    .card { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; }
    .card h2 { font-size: 1rem; font-weight: 600; color: #a5b4fc; margin-bottom: 1.2rem;
               display: flex; align-items: center; gap: .5rem; }
    .chart-container { position: relative; }
    table { width: 100%%; border-collapse: collapse; font-size: .85rem; }
    thead th { background: #0f172a; color: #94a3b8; padding: .65rem 1rem; text-align: left;
               font-weight: 600; text-transform: uppercase; letter-spacing: .04em; font-size: .75rem; }
    tbody tr { border-bottom: 1px solid #1e293b; transition: background .15s; }
    tbody tr:hover { background: #1e293b88; }
    tbody td { padding: .6rem 1rem; }
    .badge { display: inline-block; padding: .2rem .55rem; border-radius: 999px; font-size: .72rem; font-weight: 600; }
    .badge-red    { background: #7f1d1d; color: #fca5a5; }
    .badge-yellow { background: #78350f; color: #fcd34d; }
    .badge-blue   { background: #1e3a5f; color: #93c5fd; }
    .badge-green  { background: #064e3b; color: #6ee7b7; }
    .time-bar { height: 6px; border-radius: 3px; background: #334155; margin-top: 4px; overflow: hidden; }
    .time-fill { height: 100%%; border-radius: 3px; }
    .warn-item { background: #1e293b; border-left: 3px solid #f59e0b;
                 border-radius: 8px; padding: 1rem 1.25rem; margin-bottom: .75rem; }
    .warn-item.red { border-left-color: #ef4444; }
    .warn-item h4 { font-size: .9rem; font-weight: 600; color: #fbbf24; }
    .warn-item h4.red { color: #f87171; }
    .warn-item p  { font-size: .82rem; color: #94a3b8; margin-top: .3rem; }
    .warn-item .rec { font-size: .8rem; color: #6ee7b7; margin-top: .4rem; font-style: italic; }
    .lazy-chip { display: inline-block; background: #0f2a1e; color: #6ee7b7; border: 1px solid #065f46;
                 border-radius: 6px; padding: .25rem .7rem; font-size: .78rem; margin: .25rem; font-family: monospace; }
    .section-title { font-size: 1rem; font-weight: 700; color: #a5b4fc; margin: 2rem 0 1rem;
                     display: flex; align-items: center; gap: .5rem; }
    .empty-state { color: #4b5563; text-align: center; padding: 2rem; font-size: .9rem; }
    .footer { text-align: center; padding: 2rem; color: #475569; font-size: .8rem; border-top: 1px solid #1e293b; }
    .search-box { width: 100%%; background: #0f172a; border: 1px solid #334155; color: #e2e8f0;
                  padding: .5rem 1rem; border-radius: 8px; margin-bottom: 1rem; font-size: .875rem; }
    .search-box:focus { outline: none; border-color: #6366f1; }
    @media (max-width: 768px) { .charts-grid { grid-template-columns: 1fr; } }
  </style>
</head>
<body>

<div class="header">
  <h1>🚀 SpringBoot Startup Analyzer</h1>
  <p>Application: <strong>%s</strong> &nbsp;|&nbsp; Generated: <strong>%s</strong>
     &nbsp;|&nbsp; Total Startup: <strong>%dms</strong></p>
</div>

<div class="container">

  <!-- ── Stats Row ─────────────────────────────── -->
  <div class="stats-grid">
    <div class="stat-card"><div class="value v-blue">%d</div><div class="label">Total Beans</div></div>
    <div class="stat-card"><div class="value v-red">%d</div><div class="label">Slow Beans</div></div>
    <div class="stat-card"><div class="value v-yellow">%d</div><div class="label">Warnings</div></div>
    <div class="stat-card"><div class="value v-green">%d</div><div class="label">Startup (ms)</div></div>
    <div class="stat-card"><div class="value v-purple">%.0f</div><div class="label">Avg Bean Init (ms)</div></div>
    <div class="stat-card"><div class="value v-blue">%d</div><div class="label">Lazy Candidates</div></div>
  </div>

  <!-- ── Charts ────────────────────────────────── -->
  <div class="charts-grid">
    <div class="card">
      <h2>🐢 Top Slowest Beans</h2>
      <div class="chart-container"><canvas id="barChart" height="120"></canvas></div>
    </div>
    <div class="card">
      <h2>📊 Beans by Category</h2>
      <div class="chart-container"><canvas id="pieChart" height="200"></canvas></div>
    </div>
  </div>

  <!-- ── All Beans Table ───────────────────────── -->
  <div class="card">
    <h2>📋 All Beans — Initialization Times</h2>
    <input class="search-box" id="beanSearch" placeholder="🔍  Search beans..." onkeyup="filterTable()"/>
    <table id="beanTable">
      <thead>
        <tr>
          <th>#</th>
          <th>Bean Name</th>
          <th>Class</th>
          <th>Category</th>
          <th>Time (ms)</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        %s
      </tbody>
    </table>
  </div>

  <!-- ── Warnings ──────────────────────────────── -->
  %s

  <!-- ── AutoConfig Warnings ───────────────────── -->
  %s

  <!-- ── Lazy Load Suggestions ─────────────────── -->
  %s

</div>

<div class="footer">
  Generated by <strong>SpringBoot Startup Analyzer v1.0.0</strong>
  &nbsp;|&nbsp; <a href="https://github.com/anishraj/springboot-startup-analyzer"
  style="color:#6366f1">github.com/anishraj/springboot-startup-analyzer</a>
</div>

<script>
  // ── Bar Chart ────────────────────────────────────────────────
  new Chart(document.getElementById('barChart'), {
    type: 'bar',
    data: {
      labels: [%s],
      datasets: [{
        label: 'Init Time (ms)',
        data: [%s],
        backgroundColor: [%s],
        borderRadius: 6,
        borderSkipped: false,
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { display: false },
                 tooltip: { callbacks: { label: ctx => ctx.parsed.y + ' ms' } } },
      scales: {
        x: { ticks: { color: '#94a3b8', maxRotation: 30 }, grid: { color: '#1e293b' } },
        y: { ticks: { color: '#94a3b8', callback: v => v + 'ms' }, grid: { color: '#1e293b' } }
      }
    }
  });

  // ── Pie Chart ─────────────────────────────────────────────────
  const PALETTE = ['#6366f1','#8b5cf6','#06b6d4','#10b981','#f59e0b',
                   '#ef4444','#ec4899','#84cc16','#f97316','#14b8a6'];
  new Chart(document.getElementById('pieChart'), {
    type: 'doughnut',
    data: {
      labels: [%s],
      datasets: [{ data: [%s], backgroundColor: PALETTE, borderWidth: 2, borderColor: '#0f172a' }]
    },
    options: {
      responsive: true,
      plugins: {
        legend: { position: 'bottom', labels: { color: '#94a3b8', padding: 12, font: { size: 11 } } }
      }
    }
  });

  // ── Table Search ──────────────────────────────────────────────
  function filterTable() {
    const q = document.getElementById('beanSearch').value.toLowerCase();
    document.querySelectorAll('#beanTable tbody tr').forEach(row => {
      row.style.display = row.textContent.toLowerCase().includes(q) ? '' : 'none';
    });
  }
</script>
</body>
</html>
""".formatted(
                report.getApplicationName(),
                report.getApplicationName(),
                report.getFormattedGeneratedAt(),
                report.getTotalStartupTimeMs(),
                report.getTotalBeanCount(),
                report.getSlowBeanCount(),
                report.getWarningCount(),
                report.getTotalStartupTimeMs(),
                report.getAverageBeanInitTimeMs(),
                report.getLazyLoadCandidates().size(),
                buildBeanTableRows(report),
                buildBlockingWarningsSection(report),
                buildAutoConfigSection(report),
                buildLazySection(report),
                barLabels, barData, barColors,
                pieLabels, pieData
        );
    }

    private String buildBeanTableRows(StartupAnalysisReport report) {
        long maxTime = report.getBeanMetricsSorted().stream()
                .mapToLong(BeanMetric::getInitTimeMs).max().orElse(1);

        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (BeanMetric b : report.getBeanMetricsSorted()) {
            long pct = (b.getInitTimeMs() * 100) / Math.max(maxTime, 1);
            String color = b.getInitTimeMs() > 500 ? "#ef4444"
                    : b.getInitTimeMs() > 200 ? "#f59e0b" : "#6366f1";
            String badge = b.isSlow()
                    ? "<span class='badge badge-red'>SLOW</span>"
                    : "<span class='badge badge-green'>OK</span>";
            sb.append("""
                    <tr>
                      <td style="color:#4b5563">%d</td>
                      <td style="font-family:monospace;font-size:.8rem">%s</td>
                      <td style="color:#64748b;font-size:.75rem">%s</td>
                      <td><span class="badge badge-blue">%s</span></td>
                      <td>
                        <span style="color:%s;font-weight:600">%dms</span>
                        <div class="time-bar"><div class="time-fill" style="width:%d%%;background:%s"></div></div>
                      </td>
                      <td>%s</td>
                    </tr>
                    """.formatted(
                    i++,
                    escapeHtml(truncate(b.getBeanName(), 45)),
                    escapeHtml(truncate(shortClassName(b.getClassName()), 45)),
                    escapeHtml(b.getCategory()),
                    color, b.getInitTimeMs(), pct, color,
                    badge
            ));
        }
        return sb.toString();
    }

    private String buildBlockingWarningsSection(StartupAnalysisReport report) {
        List<BlockingCallWarning> warnings = report.getBlockingCallWarnings();
        if (warnings.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='section-title'>⚠️  Blocking Call Warnings</div>");
        for (BlockingCallWarning w : warnings) {
            boolean isDb = w.getType() == BlockingCallWarning.Type.DATABASE_CONNECTION;
            sb.append("""
                    <div class="warn-item %s">
                      <h4 class="%s">%s &nbsp; <span style="font-weight:400;font-family:monospace">%s</span></h4>
                      <p>%s</p>
                      <p class="rec">💡 %s</p>
                    </div>
                    """.formatted(
                    isDb ? "red" : "",
                    isDb ? "red" : "",
                    escapeHtml(w.getTypeLabel()),
                    escapeHtml(truncate(w.getBeanName(), 55)),
                    escapeHtml(w.getDescription()),
                    escapeHtml(w.getRecommendation())
            ));
        }
        return sb.toString();
    }

    private String buildAutoConfigSection(StartupAnalysisReport report) {
        List<AutoConfigWarning> warnings = report.getAutoConfigWarnings();
        if (warnings.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='section-title'>🔧  Unnecessary AutoConfiguration</div>");
        for (AutoConfigWarning w : warnings) {
            sb.append("""
                    <div class="warn-item">
                      <h4>%s</h4>
                      <p>%s</p>
                      <p class="rec">💡 %s</p>
                    </div>
                    """.formatted(
                    escapeHtml(w.getShortName()),
                    escapeHtml(w.getReason()),
                    escapeHtml(w.getSuggestion())
            ));
        }
        return sb.toString();
    }

    private String buildLazySection(StartupAnalysisReport report) {
        List<String> candidates = report.getLazyLoadCandidates();
        if (candidates.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='section-title'>💡  Lazy Load Candidates</div>");
        sb.append("<div class='card'>");
        sb.append("<p style='color:#94a3b8;margin-bottom:1rem;font-size:.85rem'>")
          .append("These beans could use <code style='background:#0f172a;padding:2px 6px;border-radius:4px'>@Lazy</code> ")
          .append("or you can enable global lazy init with <code style='background:#0f172a;padding:2px 6px;border-radius:4px'>spring.main.lazy-initialization=true</code></p>");
        for (String name : candidates) {
            sb.append("<span class='lazy-chip'>@Lazy " + escapeHtml(name) + "</span>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    // ─── Utils ────────────────────────────────────────────────────────────────

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String escapeJs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'");
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private String shortClassName(String fqcn) {
        if (fqcn == null) return "";
        int idx = fqcn.lastIndexOf('.');
        return idx >= 0 ? fqcn.substring(idx + 1) : fqcn;
    }
}
