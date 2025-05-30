package org.mlesyk.automation.performance;

import org.mlesyk.automation.utils.LoggerUtil;
import org.aeonbits.owner.ConfigFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PerformanceTestManager {

    private final PerformanceConfig config;
    private final K6TestRunner k6Runner;
    private final List<PerformanceTestResult> testResults;

    public PerformanceTestManager() {
        this.config = ConfigFactory.create(PerformanceConfig.class);
        this.k6Runner = new K6TestRunner(config);
        this.testResults = new ArrayList<>();

        // Ensure reports directory exists
        new File(config.reportsDirectory()).mkdirs();

        LoggerUtil.logFrameworkInfo("PerformanceTestManager initialized");
        LoggerUtil.info("Reports directory: {}", config.reportsDirectory());
    }

    public PerformanceTestResult runLoadTest(String baseUrl, int users, int durationSeconds) {
        LoggerUtil.info("Starting load test - Users: {}, Duration: {}s, URL: {}", users, durationSeconds, baseUrl);

        // Generate k6 script for load test
        String scriptPath = generateK6LoadTestScript(baseUrl, users, durationSeconds);
        String testName = "load_test_" + users + "u_" + durationSeconds + "s_" + getCurrentTimestamp();

        PerformanceTestResult result = k6Runner.runK6Test(scriptPath, testName);
        result.setTestType("LOAD");

        // Validate against thresholds
        validateTestResult(result);
        testResults.add(result);

        LoggerUtil.logPerformanceMetric("Load Test Throughput", result.getThroughput(), "req/s");
        LoggerUtil.logPerformanceMetric("Load Test P95", result.getP95ResponseTime(), "ms");
        LoggerUtil.logPerformanceMetric("Load Test Error Rate", result.getErrorRate(), "%");

        return result;
    }

    public PerformanceTestResult runStressTest(String baseUrl, int maxUsers, int durationSeconds) {
        LoggerUtil.info("Starting stress test - Max Users: {}, Duration: {}s, URL: {}", maxUsers, durationSeconds, baseUrl);

        String scriptPath = generateK6StressTestScript(baseUrl, maxUsers, durationSeconds);
        String testName = "stress_test_" + maxUsers + "u_" + durationSeconds + "s_" + getCurrentTimestamp();

        PerformanceTestResult result = k6Runner.runK6Test(scriptPath, testName);
        result.setTestType("STRESS");

        validateTestResult(result);
        testResults.add(result);

        LoggerUtil.logPerformanceMetric("Stress Test Peak Throughput", result.getThroughput(), "req/s");
        LoggerUtil.logPerformanceMetric("Stress Test P99", result.getP99ResponseTime(), "ms");

        return result;
    }

    public PerformanceTestResult runSpikeTest(String baseUrl, int spikeUsers, int spikeDurationSeconds) {
        LoggerUtil.info("Starting spike test - Spike Users: {}, Duration: {}s, URL: {}", spikeUsers, spikeDurationSeconds, baseUrl);

        String scriptPath = generateK6SpikeTestScript(baseUrl, spikeUsers, spikeDurationSeconds);
        String testName = "spike_test_" + spikeUsers + "u_" + spikeDurationSeconds + "s_" + getCurrentTimestamp();

        PerformanceTestResult result = k6Runner.runK6Test(scriptPath, testName);
        result.setTestType("SPIKE");

        validateTestResult(result);
        testResults.add(result);

        LoggerUtil.logPerformanceMetric("Spike Test Recovery Time", result.getAverageResponseTime(), "ms");

        return result;
    }

    private void validateTestResult(PerformanceTestResult result) {
        boolean passed = result.isWithinThresholds(config);
        result.setPassed(passed);

        if (!passed) {
            StringBuilder failureReason = new StringBuilder();

            if (result.getErrorRate() > config.errorRateThreshold()) {
                failureReason.append("Error rate ").append(result.getErrorRate())
                        .append("% exceeds threshold ").append(config.errorRateThreshold()).append("%. ");
            }

            if (result.getP95ResponseTime() > config.responseTimeP95Threshold()) {
                failureReason.append("P95 response time ").append(result.getP95ResponseTime())
                        .append("ms exceeds threshold ").append(config.responseTimeP95Threshold()).append("ms. ");
            }

            if (result.getThroughput() < config.minimumThroughput()) {
                failureReason.append("Throughput ").append(result.getThroughput())
                        .append(" req/s below minimum ").append(config.minimumThroughput()).append(" req/s. ");
            }

            result.setFailureReason(failureReason.toString());
            LoggerUtil.error("Performance test failed: {}", result.getFailureReason());
        } else {
            LoggerUtil.info("Performance test passed all thresholds");
        }
    }

    private String generateK6LoadTestScript(String baseUrl, int users, int durationSeconds) {
        String scriptContent = String.format("""
            import http from 'k6/http';
            import { check, sleep } from 'k6';
            import { Rate } from 'k6/metrics';
            
            export let errorRate = new Rate('errors');
            
            export let options = {
                stages: [
                    { duration: '30s', target: %d },     // Ramp up
                    { duration: '%ds', target: %d },     // Stay at load
                    { duration: '30s', target: 0 },      // Ramp down
                ],
                thresholds: {
                    http_req_duration: ['p(95)<2000'],
                    errors: ['rate<0.01'],
                },
            };
            
            export default function() {
                let response = http.get('%s/posts');
                check(response, {
                    'status is 200': (r) => r.status === 200,
                    'response time < 2000ms': (r) => r.timings.duration < 2000,
                });
                errorRate.add(response.status !== 200);
                sleep(1);
            }
            """, users, durationSeconds, users, baseUrl);

        return saveScriptToFile(scriptContent, "load_test_script.js");
    }

    private String generateK6StressTestScript(String baseUrl, int maxUsers, int durationSeconds) {
        String scriptContent = String.format("""
            import http from 'k6/http';
            import { check, sleep } from 'k6';
            import { Rate } from 'k6/metrics';
            
            export let errorRate = new Rate('errors');
            
            export let options = {
                stages: [
                    { duration: '60s', target: %d },     // Ramp up to stress level
                    { duration: '%ds', target: %d },     // Stay at stress level
                    { duration: '60s', target: 0 },      // Ramp down
                ],
                thresholds: {
                    http_req_duration: ['p(99)<5000'],
                    errors: ['rate<0.05'],
                },
            };
            
            export default function() {
                let responses = http.batch([
                    ['GET', '%s/posts'],
                    ['GET', '%s/users'],
                    ['GET', '%s/comments'],
                ]);
                
                for (let response of responses) {
                    check(response, {
                        'status is 200': (r) => r.status === 200,
                    });
                    errorRate.add(response.status !== 200);
                }
                
                sleep(Math.random() * 2);
            }
            """, maxUsers, durationSeconds, maxUsers, baseUrl, baseUrl, baseUrl);

        return saveScriptToFile(scriptContent, "stress_test_script.js");
    }

    private String generateK6SpikeTestScript(String baseUrl, int spikeUsers, int spikeDurationSeconds) {
        String scriptContent = String.format("""
            import http from 'k6/http';
            import { check, sleep } from 'k6';
            import { Rate } from 'k6/metrics';
            
            export let errorRate = new Rate('errors');
            
            export let options = {
                stages: [
                    { duration: '10s', target: 10 },     // Normal load
                    { duration: '10s', target: %d },     // Spike!
                    { duration: '%ds', target: %d },     // Stay at spike
                    { duration: '10s', target: 10 },     // Back to normal
                    { duration: '10s', target: 0 },      // Ramp down
                ],
                thresholds: {
                    http_req_duration: ['p(95)<3000'],
                    errors: ['rate<0.1'],
                },
            };
            
            export default function() {
                let response = http.get('%s/posts/' + Math.floor(Math.random() * 100 + 1));
                check(response, {
                    'status is 200': (r) => r.status === 200,
                    'response time < 3000ms': (r) => r.timings.duration < 3000,
                });
                errorRate.add(response.status !== 200);
                sleep(0.5);
            }
            """, spikeUsers, spikeDurationSeconds, spikeUsers, baseUrl);

        return saveScriptToFile(scriptContent, "spike_test_script.js");
    }

    private String saveScriptToFile(String content, String filename) {
        try {
            File scriptDir = new File(config.reportsDirectory() + "/scripts");
            scriptDir.mkdirs();

            File scriptFile = new File(scriptDir, filename);
            java.nio.file.Files.write(scriptFile.toPath(), content.getBytes());

            LoggerUtil.debug("Generated k6 script: {}", scriptFile.getAbsolutePath());
            return scriptFile.getAbsolutePath();

        } catch (Exception e) {
            LoggerUtil.error("Failed to save k6 script", e);
            throw new RuntimeException("Script generation failed", e);
        }
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    public List<PerformanceTestResult> getAllResults() {
        return new ArrayList<>(testResults);
    }

    public PerformanceTestSummary generateSummary() {
        LoggerUtil.info("Generating performance test summary for {} tests", testResults.size());

        return PerformanceTestSummary.builder()
                .totalTests(testResults.size())
                .passedTests((int) testResults.stream().filter(PerformanceTestResult::isPassed).count())
                .failedTests((int) testResults.stream().filter(r -> !r.isPassed()).count())
                .averageThroughput(testResults.stream().mapToDouble(PerformanceTestResult::getThroughput).average().orElse(0))
                .averageP95ResponseTime(testResults.stream().mapToLong(PerformanceTestResult::getP95ResponseTime).average().orElse(0))
                .averageErrorRate(testResults.stream().mapToDouble(PerformanceTestResult::getErrorRate).average().orElse(0))
                .results(testResults)
                .build();
    }
}
