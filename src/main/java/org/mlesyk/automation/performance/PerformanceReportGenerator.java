package org.mlesyk.automation.performance;

import org.mlesyk.automation.utils.LoggerUtil;
import org.mlesyk.automation.utils.TemplateEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerformanceReportGenerator {

    private final ObjectMapper objectMapper;
    private final String reportsDirectory;

    public PerformanceReportGenerator(String reportsDirectory) {
        this.reportsDirectory = reportsDirectory;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Ensure reports directory exists
        new File(reportsDirectory).mkdirs();
    }

    public void generateJSONReport(List<PerformanceTestResult> results, String reportName) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s/%s_%s.json", reportsDirectory, reportName, timestamp);

            File reportFile = new File(fileName);
            objectMapper.writeValue(reportFile, results);

            LoggerUtil.info("JSON performance report generated: {}", fileName);

        } catch (IOException e) {
            LoggerUtil.error("Failed to generate JSON performance report", e);
            throw new RuntimeException("Report generation failed", e);
        }
    }

    public void generateHTMLReport(PerformanceTestSummary summary, String reportName) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s/%s_%s.html", reportsDirectory, reportName, timestamp);

            String htmlContent = generateHTMLContentFromTemplate(summary);

            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(htmlContent);
            }

            LoggerUtil.info("HTML performance report generated: {}", fileName);

        } catch (IOException e) {
            LoggerUtil.error("Failed to generate HTML performance report", e);
            throw new RuntimeException("HTML report generation failed", e);
        }
    }

    private String generateHTMLContentFromTemplate(PerformanceTestSummary summary) {
        // Prepare template variables
        Map<String, String> variables = new HashMap<>();

        // Basic summary variables
        variables.put("GENERATION_TIME", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        variables.put("TOTAL_TESTS", String.valueOf(summary.getTotalTests()));
        variables.put("PASS_RATE", TemplateEngine.formatDecimal(summary.getPassRate(), 1));
        variables.put("AVG_THROUGHPUT", TemplateEngine.formatDecimal(summary.getAverageThroughput(), 1));
        variables.put("AVG_P95", TemplateEngine.formatDecimal(summary.getAverageP95ResponseTime(), 0));
        variables.put("AVG_ERROR_RATE", TemplateEngine.formatDecimal(summary.getAverageErrorRate(), 2));

        // Performance analysis variables
        long totalRequests = summary.getResults().stream().mapToLong(PerformanceTestResult::getTotalRequests).sum();
        double overallSuccessRate = summary.getResults().stream().mapToDouble(PerformanceTestResult::getSuccessRate).average().orElse(0);
        double peakThroughput = summary.getResults().stream().mapToDouble(PerformanceTestResult::getThroughput).max().orElse(0);

        variables.put("TOTAL_REQUESTS", TemplateEngine.formatNumber(totalRequests));
        variables.put("OVERALL_SUCCESS_RATE", TemplateEngine.formatDecimal(overallSuccessRate, 1));
        variables.put("PEAK_THROUGHPUT", TemplateEngine.formatDecimal(peakThroughput, 1));
        variables.put("BASELINE_STATUS", summary.getPassedTests() > 0 ? "ESTABLISHED" : "NEEDS ATTENTION");

        // Generate test results rows
        variables.put("TEST_RESULTS_ROWS", generateTestResultsRows(summary.getResults()));

        // Generate failed tests section if needed
        if (summary.getFailedTests() > 0) {
            variables.put("FAILED_TESTS_SECTION", generateFailedTestsSection(summary.getResults()));
        } else {
            variables.put("FAILED_TESTS_SECTION", "");
        }

        // Generate recommendations
        variables.put("RECOMMENDATIONS", generateRecommendations(summary));

        // Load and process main template
        return TemplateEngine.loadAndProcessTemplate("templates/performance-report.html", variables);
    }

    private String generateTestResultsRows(List<PerformanceTestResult> results) {
        StringBuilder rows = new StringBuilder();

        for (PerformanceTestResult result : results) {
            Map<String, String> rowVariables = new HashMap<>();

            rowVariables.put("TEST_NAME", TemplateEngine.escapeHtml(result.getTestName()));
            rowVariables.put("TEST_TYPE", result.getTestType());
            rowVariables.put("STATUS_CLASS", result.isPassed() ? "status-passed" : "status-failed");
            rowVariables.put("STATUS_TEXT", result.isPassed() ? "✅ PASSED" : "❌ FAILED");
            rowVariables.put("DURATION", String.valueOf(result.getDurationSeconds()));
            rowVariables.put("TOTAL_REQUESTS", TemplateEngine.formatNumber(result.getTotalRequests()));
            rowVariables.put("SUCCESS_RATE", TemplateEngine.formatDecimal(result.getSuccessRate(), 2));
            rowVariables.put("THROUGHPUT", TemplateEngine.formatDecimal(result.getThroughput(), 2));
            rowVariables.put("AVG_RESPONSE_TIME", String.valueOf(result.getAverageResponseTime()));
            rowVariables.put("P95_RESPONSE_TIME", String.valueOf(result.getP95ResponseTime()));
            rowVariables.put("P99_RESPONSE_TIME", String.valueOf(result.getP99ResponseTime()));
            rowVariables.put("ERROR_RATE", TemplateEngine.formatDecimal(result.getErrorRate(), 2));

            String rowHtml = TemplateEngine.loadAndProcessTemplate("templates/test-result-row.html", rowVariables);
            rows.append(rowHtml).append("\n");
        }

        return rows.toString();
    }

    private String generateFailedTestsSection(List<PerformanceTestResult> results) {
        StringBuilder failedRows = new StringBuilder();

        for (PerformanceTestResult result : results) {
            if (!result.isPassed()) {
                Map<String, String> rowVariables = new HashMap<>();

                rowVariables.put("TEST_NAME", TemplateEngine.escapeHtml(result.getTestName()));
                rowVariables.put("FAILURE_REASON", TemplateEngine.escapeHtml(
                        result.getFailureReason() != null ? result.getFailureReason() : "Threshold exceeded"));
                rowVariables.put("ERROR_RATE", TemplateEngine.formatDecimal(result.getErrorRate(), 2));
                rowVariables.put("P95_RESPONSE_TIME", String.valueOf(result.getP95ResponseTime()));
                rowVariables.put("THROUGHPUT", TemplateEngine.formatDecimal(result.getThroughput(), 2));

                String rowHtml = TemplateEngine.loadAndProcessTemplate("templates/failed-test-row.html", rowVariables);
                failedRows.append(rowHtml).append("\n");
            }
        }

        Map<String, String> sectionVariables = new HashMap<>();
        sectionVariables.put("FAILED_TEST_ROWS", failedRows.toString());

        return TemplateEngine.loadAndProcessTemplate("templates/failed-tests-section.html", sectionVariables);
    }

    private String generateRecommendations(PerformanceTestSummary summary) {
        StringBuilder recommendations = new StringBuilder();

        if (summary.getPassRate() >= 90) {
            recommendations.append("""
                    <div class="recommendation-success">
                        <strong>✅ Excellent Performance:</strong> System is performing well within acceptable thresholds.
                        Consider establishing this as your performance baseline.
                    </div>
                """);
        } else if (summary.getPassRate() >= 70) {
            recommendations.append("""
                    <div class="recommendation-warning">
                        <strong>⚠️ Performance Concerns:</strong> Some tests are failing thresholds.
                        Review failed tests and consider optimizing critical paths.
                    </div>
                """);
        } else {
            recommendations.append("""
                    <div class="recommendation-danger">
                        <strong>🚨 Performance Issues:</strong> Significant performance degradation detected.
                        Immediate investigation and optimization required.
                    </div>
                """);
        }

        return recommendations.toString();
    }

    public void generateCSVReport(List<PerformanceTestResult> results, String reportName) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s/%s_%s.csv", reportsDirectory, reportName, timestamp);

            try (FileWriter writer = new FileWriter(fileName)) {
                // CSV Header
                writer.write("Test Name,Type,Status,Start Time,Duration (s),Total Requests,Successful,Failed," +
                        "Error Rate (%),Throughput (req/s),Avg Response (ms),P95 (ms),P99 (ms)\n");

                // CSV Data
                for (PerformanceTestResult result : results) {
                    writer.write(String.format("%s,%s,%s,%s,%d,%d,%d,%d,%.2f,%.2f,%d,%d,%d\n",
                            result.getTestName(),
                            result.getTestType(),
                            result.isPassed() ? "PASSED" : "FAILED",
                            result.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            result.getDurationSeconds(),
                            result.getTotalRequests(),
                            result.getSuccessfulRequests(),
                            result.getFailedRequests(),
                            result.getErrorRate(),
                            result.getThroughput(),
                            result.getAverageResponseTime(),
                            result.getP95ResponseTime(),
                            result.getP99ResponseTime()
                    ));
                }
            }

            LoggerUtil.info("CSV performance report generated: {}", fileName);

        } catch (IOException e) {
            LoggerUtil.error("Failed to generate CSV performance report", e);
            throw new RuntimeException("CSV report generation failed", e);
        }
    }
}
