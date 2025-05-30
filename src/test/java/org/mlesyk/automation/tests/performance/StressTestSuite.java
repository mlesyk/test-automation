package org.mlesyk.automation.tests.performance;

import org.mlesyk.automation.performance.PerformanceTestResult;
import org.mlesyk.automation.utils.LoggerUtil;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Epic("Performance Testing")
@Feature("Stress Testing")
public class StressTestSuite extends PerformanceBaseTest {

    @Test(description = "Basic stress test to find breaking point")
    @Story("Stress Test - Breaking Point")
    @Severity(SeverityLevel.CRITICAL)
    public void testBasicStressTest() {
        LoggerUtil.info("Starting basic stress test");

        int users = performanceConfig.stressTestUsers();
        int duration = performanceConfig.stressTestDurationSeconds();

        PerformanceTestResult result = performanceManager.runStressTest(targetBaseUrl, users, duration);

        // Validate test execution
        assertNotNull(result, "Test result should not be null");
        assertEquals(result.getTestType(), "STRESS", "Test type should be STRESS");
        assertTrue(result.getTotalRequests() > 0, "Total requests should be greater than 0");

        // For stress tests, we expect some degradation but system should still respond
        assertTrue(result.getErrorRate() <= 10.0,
                String.format("Error rate %.2f%% should be <= 10%% under stress", result.getErrorRate()));

        assertTrue(result.getP99ResponseTime() <= performanceConfig.responseTimeP99Threshold(),
                String.format("P99 response time %dms should be <= %dms under stress",
                        result.getP99ResponseTime(), performanceConfig.responseTimeP99Threshold()));

        LoggerUtil.info("Stress test completed");
        LoggerUtil.logPerformanceMetric("Stress Test Max Response Time", result.getMaxResponseTime(), "ms");
        LoggerUtil.logPerformanceMetric("Stress Test Throughput", result.getThroughput(), "req/s");
    }

    @Test(description = "Progressive stress test with ramping users")
    @Story("Stress Test - Progressive")
    @Severity(SeverityLevel.NORMAL)
    public void testProgressiveStressTest() {
        LoggerUtil.info("Starting progressive stress test");

        // Test with multiple user levels to find the breaking point
        int[] userLevels = {25, 50, 75, 100};
        int duration = 60; // Shorter duration for each level

        for (int users : userLevels) {
            LoggerUtil.info("Testing with {} users", users);

            PerformanceTestResult result = performanceManager.runStressTest(targetBaseUrl, users, duration);

            assertNotNull(result, "Test result should not be null");

            LoggerUtil.logPerformanceMetric("Users " + users + " - Throughput", result.getThroughput(), "req/s");
            LoggerUtil.logPerformanceMetric("Users " + users + " - P95", result.getP95ResponseTime(), "ms");
            LoggerUtil.logPerformanceMetric("Users " + users + " - Error Rate", result.getErrorRate(), "%");

            // Log when we start seeing significant degradation
            if (result.getErrorRate() > 5.0) {
                LoggerUtil.warn("Significant error rate detected at {} users: {}%", users, result.getErrorRate());
            }

            if (result.getP95ResponseTime() > performanceConfig.responseTimeP95Threshold() * 2) {
                LoggerUtil.warn("Response time degradation at {} users: {}ms", users, result.getP95ResponseTime());
            }
        }

        LoggerUtil.info("Progressive stress test completed");
    }
}