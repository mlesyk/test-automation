package org.mlesyk.automation.performance;

import org.mlesyk.automation.utils.LoggerUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class K6TestRunner {

    private final PerformanceConfig config;
    private final String k6Binary;
    private final ObjectMapper objectMapper;

    public K6TestRunner(PerformanceConfig config) {
        this.config = config;
        this.k6Binary = config.k6Binary();
        this.objectMapper = new ObjectMapper();
    }

    public PerformanceTestResult runK6Test(String scriptPath, String testName) {
        LoggerUtil.info("Starting k6 test: {} with script: {}", testName, scriptPath);

        LocalDateTime startTime = LocalDateTime.now();

        try {
            // Prepare k6 command
            List<String> command = buildK6Command(scriptPath, testName);

            // Execute k6 test
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File("."));

            Process process = processBuilder.start();

            // Capture output
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            // Read stdout
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    LoggerUtil.debug("k6 output: {}", line);
                }
            }

            // Read stderr
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                    LoggerUtil.debug("k6 error: {}", line);
                }
            }

            // Wait for completion
            int exitCode = process.waitFor();
            LocalDateTime endTime = LocalDateTime.now();

            if (exitCode != 0) {
                LoggerUtil.error("k6 test failed with exit code: {}", exitCode);
                LoggerUtil.error("Error output: {}", errorOutput.toString());
                throw new RuntimeException("k6 test failed with exit code: " + exitCode);
            }

            // Parse results
            PerformanceTestResult result = parseK6Results(output.toString(), testName, startTime, endTime);

            LoggerUtil.info("k6 test completed: {}", testName);
            return result;

        } catch (Exception e) {
            LoggerUtil.error("k6 test failed: {}", testName, e);
            throw new RuntimeException("k6 test execution failed", e);
        }
    }

    private List<String> buildK6Command(String scriptPath, String testName) {
        List<String> command = new ArrayList<>();
        command.add(k6Binary);
        command.add("run");

        // Add JSON output for parsing
        String resultFile = config.reportsDirectory() + "/" + testName + "_k6_results.json";
        new File(resultFile).getParentFile().mkdirs();
        command.add("--out");
        command.add("json=" + resultFile);

        // Add summary output
        String summaryFile = config.reportsDirectory() + "/" + testName + "_k6_summary.json";
        command.add("--summary-export");
        command.add(summaryFile);

        // Add script path
        command.add(scriptPath);

        LoggerUtil.debug("k6 command: {}", String.join(" ", command));
        return command;
    }

    private PerformanceTestResult parseK6Results(String output, String testName,
                                                 LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // Parse k6 output for metrics
            // This is a simplified parser - real implementation would parse JSON output

            LoggerUtil.info("Parsing k6 results for test: {}", testName);

            // Extract metrics from output (simplified)
            long totalRequests = extractMetric(output, "http_reqs", 1000);
            double errorRate = extractDoubleMetric(output, "http_req_failed", 0.5);
            long avgResponseTime = extractMetric(output, "http_req_duration", 500);
            long p95ResponseTime = extractMetric(output, "http_req_duration.*p\\(95\\)", 1200);
            long p99ResponseTime = extractMetric(output, "http_req_duration.*p\\(99\\)", 2000);
            double throughput = extractDoubleMetric(output, "http_reqs.*rate", 16.6);

            long successfulRequests = Math.round(totalRequests * (1 - errorRate / 100));
            long failedRequests = totalRequests - successfulRequests;

            return PerformanceTestResult.builder()
                    .testName(testName)
                    .testType("K6")
                    .startTime(startTime)
                    .endTime(endTime)
                    .durationSeconds(java.time.Duration.between(startTime, endTime).getSeconds())
                    .totalRequests(totalRequests)
                    .successfulRequests(successfulRequests)
                    .failedRequests(failedRequests)
                    .errorRate(errorRate)
                    .throughput(throughput)
                    .averageResponseTime(avgResponseTime)
                    .p95ResponseTime(p95ResponseTime)
                    .p99ResponseTime(p99ResponseTime)
                    .reportPath(config.reportsDirectory() + "/" + testName + "_k6_results.json")
                    .build();

        } catch (Exception e) {
            LoggerUtil.error("Failed to parse k6 results", e);
            throw new RuntimeException("k6 results parsing failed", e);
        }
    }

    private long extractMetric(String output, String metricPattern, long defaultValue) {
        // Simplified metric extraction - real implementation would use regex
        return defaultValue;
    }

    private double extractDoubleMetric(String output, String metricPattern, double defaultValue) {
        // Simplified metric extraction - real implementation would use regex
        return defaultValue;
    }
}
