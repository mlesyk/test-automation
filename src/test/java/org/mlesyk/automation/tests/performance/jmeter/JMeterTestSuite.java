package org.mlesyk.automation.tests.performance.jmeter;

import org.mlesyk.automation.performance.JMeterTestRunner;
import org.mlesyk.automation.performance.PerformanceTestResult;
import org.mlesyk.automation.tests.performance.PerformanceBaseTest;
import org.mlesyk.automation.utils.LoggerUtil;
import org.mlesyk.automation.utils.PerformanceAssertions;
import io.qameta.allure.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Properties;

import static org.testng.Assert.*;

@Epic("Performance Testing")
@Feature("JMeter Integration")
public class JMeterTestSuite extends PerformanceBaseTest {

    private JMeterTestRunner jmeterRunner;
    private String testPlanPath;

    @BeforeClass
    public void setUpJMeter() {
        jmeterRunner = new JMeterTestRunner(performanceConfig);

        // Set up test plan path
        testPlanPath = "src/test/jmeter/api-load-test.jmx";

        // Verify test plan exists
        File testPlanFile = new File(testPlanPath);
        if (!testPlanFile.exists()) {
            LoggerUtil.warn("JMeter test plan not found: {}", testPlanPath);
            LoggerUtil.info("Create the test plan file or skip JMeter tests");
        }

        LoggerUtil.logFrameworkInfo("JMeter test suite initialized");
    }

    @Test(description = "Basic JMeter load test")
    @Story("JMeter Load Testing")
    @Severity(SeverityLevel.CRITICAL)
    public void testJMeterLoadTest() {
        LoggerUtil.info("Starting JMeter load test");

        // Skip if test plan doesn't exist
        File testPlanFile = new File(testPlanPath);
        if (!testPlanFile.exists()) {
            LoggerUtil.warn("Skipping JMeter test - test plan not found: {}", testPlanPath);
            return;
        }

        // Create test properties
        Properties testProps = JMeterTestRunner.createTestProperties(
                targetBaseUrl,
                performanceConfig.loadTestUsers(),
                performanceConfig.loadTestDurationSeconds()
        );

        // Run JMeter test
        PerformanceTestResult result = jmeterRunner.runTestPlan(testPlanPath, "jmeter_load_test", testProps);

        // Validate results
        assertNotNull(result, "JMeter result should not be null");
        assertEquals(result.getTestType(), "JMETER", "Test type should be JMETER");

        // Use PerformanceAssertions for validation
        if (result.getTotalRequests() > 0) {
            PerformanceAssertions.assertSuccessRate(result, 95.0);
            PerformanceAssertions.assertResponseTime(result, 2000, "p95");
            PerformanceAssertions.assertErrorRate(result, 5.0);
        }

        LoggerUtil.info("JMeter load test completed");
        LoggerUtil.logPerformanceMetric("JMeter Throughput", result.getThroughput(), "req/s");
        LoggerUtil.logPerformanceMetric("JMeter P95", result.getP95ResponseTime(), "ms");
    }

    @Test(description = "JMeter stress test with custom properties")
    @Story("JMeter Stress Testing")
    @Severity(SeverityLevel.NORMAL)
    public void testJMeterStressTest() {
        LoggerUtil.info("Starting JMeter stress test");

        // Skip if test plan doesn't exist
        File testPlanFile = new File(testPlanPath);
        if (!testPlanFile.exists()) {
            LoggerUtil.warn("Skipping JMeter stress test - test plan not found: {}", testPlanPath);
            return;
        }

        // Create stress test properties
        Properties stressProps = JMeterTestRunner.createTestProperties(
                targetBaseUrl,
                performanceConfig.stressTestUsers(),
                performanceConfig.stressTestDurationSeconds(),
                60 // ramp-up time
        );

        // Add additional stress test properties
        stressProps.setProperty("think.time", "500"); // Shorter think time for stress

        // Run JMeter stress test
        PerformanceTestResult result = jmeterRunner.runTestPlan(testPlanPath, "jmeter_stress_test", stressProps);

        // Validate results with relaxed thresholds for stress test
        assertNotNull(result, "JMeter stress result should not be null");

        if (result.getTotalRequests() > 0) {
            PerformanceAssertions.assertSuccessRate(result, 85.0); // Relaxed for stress
            PerformanceAssertions.assertErrorRate(result, 15.0); // Higher error rate OK
        }

        LoggerUtil.info("JMeter stress test completed");
        LoggerUtil.logPerformanceMetric("JMeter Stress Throughput", result.getThroughput(), "req/s");
    }

    @Test(description = "JMeter baseline test for comparison")
    @Story("JMeter Baseline")
    @Severity(SeverityLevel.NORMAL)
    public void testJMeterBaseline() {
        LoggerUtil.info("Establishing JMeter baseline");

        // Skip if test plan doesn't exist
        File testPlanFile = new File(testPlanPath);
        if (!testPlanFile.exists()) {
            LoggerUtil.warn("Skipping JMeter baseline test - test plan not found: {}", testPlanPath);
            return;
        }

        // Conservative baseline properties
        Properties baselineProps = JMeterTestRunner.createTestProperties(
                targetBaseUrl,
                5, // Conservative user count
                120, // Longer duration for stable baseline
                30 // Gradual ramp-up
        );

        // Run baseline test
        PerformanceTestResult baseline = jmeterRunner.runTestPlan(testPlanPath, "jmeter_baseline", baselineProps);

        // Validate baseline with strict thresholds
        assertNotNull(baseline, "JMeter baseline result should not be null");

        if (baseline.getTotalRequests() > 0) {
            PerformanceAssertions.assertSuccessRate(baseline, 99.0); // Very high success rate
            PerformanceAssertions.assertResponseTime(baseline, 1000, "p95"); // Strict response time
            PerformanceAssertions.assertErrorRate(baseline, 1.0); // Very low error rate
        }

        LoggerUtil.info("=== JMETER BASELINE ESTABLISHED ===");
        LoggerUtil.logPerformanceMetric("JMeter Baseline Throughput", baseline.getThroughput(), "req/s");
        LoggerUtil.logPerformanceMetric("JMeter Baseline P95", baseline.getP95ResponseTime(), "ms");
        LoggerUtil.logPerformanceMetric("JMeter Baseline Error Rate", baseline.getErrorRate(), "%");

        LoggerUtil.info("JMeter baseline test completed");
    }
}
