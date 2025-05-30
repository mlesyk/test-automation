package org.mlesyk.automation.tests.performance;

import org.mlesyk.automation.performance.PerformanceTestResult;
import org.mlesyk.automation.utils.LoggerUtil;
import io.qameta.allure.*;
import org.mlesyk.automation.utils.PerformanceAssertions;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Epic("Performance Testing")
@Feature("Load Testing")
public class LoadTestSuite extends PerformanceBaseTest {

    @Test(description = "Basic load test with default users")
    @Story("Load Test - Basic")
    @Severity(SeverityLevel.CRITICAL)
    public void testBasicLoadTest() {
        LoggerUtil.info("Starting basic load test");

        int users = performanceConfig.loadTestUsers();
        int duration = performanceConfig.loadTestDurationSeconds();

        PerformanceTestResult result = performanceManager.runLoadTest(targetBaseUrl, users, duration);

        // Validate test execution
        assertNotNull(result, "Test result should not be null");
        assertEquals(result.getTestType(), "LOAD", "Test type should be LOAD");
        assertTrue(result.getTotalRequests() > 0, "Total requests should be greater than 0");

        // Use PerformanceAssertions for comprehensive validation
        PerformanceAssertions.assertPerformanceThresholds(result, performanceConfig);

        // Additional specific assertions
        PerformanceAssertions.assertResponseTime(result, 2000, "p95");
        PerformanceAssertions.assertThroughput(result, performanceConfig.minimumThroughput());
        PerformanceAssertions.assertErrorRate(result, performanceConfig.errorRateThreshold());
        PerformanceAssertions.assertSuccessRate(result, 95.0);

        LoggerUtil.info("Load test completed successfully");
        LoggerUtil.logPerformanceMetric("Test Duration", result.getDurationSeconds(), "seconds");
        LoggerUtil.logPerformanceMetric("Success Rate", result.getSuccessRate(), "%");
    }

    @Test(description = "Load test with increased user count")
    @Story("Load Test - Scaled")
    @Severity(SeverityLevel.NORMAL)
    public void testScaledLoadTest() {
        LoggerUtil.info("Starting scaled load test");

        int users = performanceConfig.loadTestUsers() * 2; // Double the users
        int duration = performanceConfig.loadTestDurationSeconds();

        PerformanceTestResult result = performanceManager.runLoadTest(targetBaseUrl, users, duration);

        // Validate test execution
        assertNotNull(result, "Test result should not be null");
        assertTrue(result.getTotalRequests() > 0, "Total requests should be greater than 0");

        // Use PerformanceAssertions with relaxed thresholds for scaled test
        PerformanceAssertions.assertResponseTime(result, 3000, "p95"); // Relaxed threshold
        PerformanceAssertions.assertErrorRate(result, 5.0); // Allow higher error rate
        PerformanceAssertions.assertSuccessRate(result, 90.0); // Lower success rate threshold

        // Log performance degradation if any
        if (result.getP95ResponseTime() > performanceConfig.responseTimeP95Threshold() * 0.8) {
            LoggerUtil.warn("Response time approaching threshold: {}ms", result.getP95ResponseTime());
        }

        if (result.getErrorRate() > performanceConfig.errorRateThreshold() * 0.5) {
            LoggerUtil.warn("Error rate increasing: {}%", result.getErrorRate());
        }

        LoggerUtil.info("Scaled load test completed");
        LoggerUtil.logPerformanceMetric("Scaled Test Throughput", result.getThroughput(), "req/s");
    }

    @Test(description = "Load test with extended duration")
    @Story("Load Test - Endurance")
    @Severity(SeverityLevel.NORMAL)
    public void testEnduranceLoadTest() {
        LoggerUtil.info("Starting endurance load test");

        int users = performanceConfig.loadTestUsers();
        int duration = performanceConfig.loadTestDurationSeconds() * 2; // Double duration

        PerformanceTestResult result = performanceManager.runLoadTest(targetBaseUrl, users, duration);

        // Validate test execution
        assertNotNull(result, "Test result should not be null");
        assertTrue(result.getTotalRequests() > 0, "Total requests should be greater than 0");

        // Use PerformanceAssertions for endurance-specific validations
        PerformanceAssertions.assertSuccessRate(result, 99.0); // High success rate for endurance
        PerformanceAssertions.assertErrorRate(result, 1.0); // Low error rate for stability
        PerformanceAssertions.assertResponseTime(result, performanceConfig.responseTimeP95Threshold(), "p95");

        // Check for memory leaks or performance degradation over time
        double successRate = result.getSuccessRate();
        assertTrue(successRate >= 99.0,
                String.format("Success rate %.2f%% should remain high during endurance test", successRate));

        LoggerUtil.info("Endurance load test completed");
        LoggerUtil.logPerformanceMetric("Endurance Success Rate", successRate, "%");
    }

    @Test(description = "Load test baseline establishment")
    @Story("Load Test - Baseline")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoadTestBaseline() {
        LoggerUtil.info("Establishing load test baseline");

        int users = 5; // Conservative user count for baseline
        int duration = 120; // Longer duration for stable baseline

        PerformanceTestResult baseline = performanceManager.runLoadTest(targetBaseUrl, users, duration);

        // Validate baseline meets all thresholds
        PerformanceAssertions.assertPerformanceThresholds(baseline, performanceConfig);

        // Baseline should be very stable
        PerformanceAssertions.assertSuccessRate(baseline, 99.5);
        PerformanceAssertions.assertErrorRate(baseline, 0.5);
        PerformanceAssertions.assertResponseTime(baseline, 1000, "p95"); // Strict baseline

        // Store baseline for future comparisons (in real implementation)
        LoggerUtil.info("=== BASELINE ESTABLISHED ===");
        LoggerUtil.logPerformanceMetric("Baseline Throughput", baseline.getThroughput(), "req/s");
        LoggerUtil.logPerformanceMetric("Baseline P95", baseline.getP95ResponseTime(), "ms");
        LoggerUtil.logPerformanceMetric("Baseline Error Rate", baseline.getErrorRate(), "%");

        LoggerUtil.info("Load test baseline established successfully");
    }
}