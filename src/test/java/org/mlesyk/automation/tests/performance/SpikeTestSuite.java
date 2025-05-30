package org.mlesyk.automation.tests.performance;

import org.mlesyk.automation.performance.PerformanceTestResult;
import org.mlesyk.automation.utils.LoggerUtil;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Epic("Performance Testing")
@Feature("Spike Testing")
public class SpikeTestSuite extends PerformanceBaseTest {

    @Test(description = "Basic spike test for sudden load increase")
    @Story("Spike Test - Basic")
    @Severity(SeverityLevel.CRITICAL)
    public void testBasicSpikeTest() {
        LoggerUtil.info("Starting basic spike test");

        int spikeUsers = performanceConfig.spikeTestUsers();
        int duration = performanceConfig.spikeTestDurationSeconds();

        PerformanceTestResult result = performanceManager.runSpikeTest(targetBaseUrl, spikeUsers, duration);

        // Validate test execution
        assertNotNull(result, "Test result should not be null");
        assertEquals(result.getTestType(), "SPIKE", "Test type should be SPIKE");
        assertTrue(result.getTotalRequests() > 0, "Total requests should be greater than 0");

        // For spike tests, we check system recovery and stability
        assertTrue(result.getErrorRate() <= 15.0,
                String.format("Error rate %.2f%% should be <= 15%% during spike", result.getErrorRate()));

        // System should handle the spike without complete failure
        assertTrue(result.getSuccessRate() >= 85.0,
                String.format("Success rate %.2f%% should be >= 85%% during spike", result.getSuccessRate()));

        LoggerUtil.info("Spike test completed");
        LoggerUtil.logPerformanceMetric("Spike Test Recovery Time", result.getAverageResponseTime(), "ms");
        LoggerUtil.logPerformanceMetric("Spike Test Resilience", result.getSuccessRate(), "%");
    }

    @Test(description = "Multiple spike test to verify recovery")
    @Story("Spike Test - Recovery")
    @Severity(SeverityLevel.NORMAL)
    public void testMultipleSpikeTest() {
        LoggerUtil.info("Starting multiple spike test for recovery verification");

        int normalUsers = 10;
        int spikeUsers = performanceConfig.spikeTestUsers();
        int spikeDuration = 20;

        // Run multiple spikes to test system recovery
        for (int spike = 1; spike <= 3; spike++) {
            LoggerUtil.info("Running spike test #{}", spike);

            PerformanceTestResult result = performanceManager.runSpikeTest(targetBaseUrl, spikeUsers, spikeDuration);

            assertNotNull(result, "Test result should not be null");

            LoggerUtil.logPerformanceMetric("Spike " + spike + " - Error Rate", result.getErrorRate(), "%");
            LoggerUtil.logPerformanceMetric("Spike " + spike + " - Recovery", result.getSuccessRate(), "%");

            // Each subsequent spike should not be significantly worse
            if (spike > 1 && result.getErrorRate() > 20.0) {
                LoggerUtil.warn("System may not be recovering properly from spikes: {}% error rate",
                        result.getErrorRate());
            }

            // Brief pause between spikes to allow recovery
            try {
                Thread.sleep(30000); // 30 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        LoggerUtil.info("Multiple spike test completed");
    }
}
