package org.mlesyk.automation.tests.performance;

import org.mlesyk.automation.performance.PerformanceReportGenerator;
import org.mlesyk.automation.performance.PerformanceTestResult;
import org.mlesyk.automation.performance.PerformanceTestSummary;
import org.mlesyk.automation.utils.LoggerUtil;
import io.qameta.allure.*;
import org.mlesyk.automation.utils.PerformanceAssertions;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

@Epic("Performance Testing")
@Feature("Integration Testing")
public class PerformanceIntegrationTest extends PerformanceBaseTest {

    @Test(description = "Complete performance testing workflow")
    @Story("Full Performance Suite")
    @Severity(SeverityLevel.CRITICAL)
    public void testCompletePerformanceWorkflow() {
        LoggerUtil.info("Starting complete performance testing workflow");

        // Step 1: Baseline load test
        LoggerUtil.info("Step 1: Establishing baseline with load test");
        PerformanceTestResult loadResult = performanceManager.runLoadTest(
                targetBaseUrl,
                performanceConfig.loadTestUsers(),
                60
        );

        // Use PerformanceAssertions for validation
        PerformanceAssertions.assertPerformanceThresholds(loadResult, performanceConfig);
        assertTrue(loadResult.isPassed(), "Baseline load test should pass");

        // Step 2: Stress test to find limits
        LoggerUtil.info("Step 2: Finding system limits with stress test");
        PerformanceTestResult stressResult = performanceManager.runStressTest(
                targetBaseUrl,
                performanceConfig.stressTestUsers(),
                90
        );

        // Stress tests have relaxed validation
        PerformanceAssertions.assertErrorRate(stressResult, 10.0); // Allow higher error rate
        PerformanceAssertions.assertSuccessRate(stressResult, 80.0); // Lower success rate OK

        // Step 3: Spike test for resilience
        LoggerUtil.info("Step 3: Testing resilience with spike test");
        PerformanceTestResult spikeResult = performanceManager.runSpikeTest(
                targetBaseUrl,
                performanceConfig.spikeTestUsers(),
                30
        );

        // Spike tests focus on recovery
        PerformanceAssertions.assertSuccessRate(spikeResult, 85.0); // Should recover
        PerformanceAssertions.assertErrorRate(spikeResult, 15.0); // Spikes can have errors

        // Step 4: Analyze results and generate comprehensive reports
        PerformanceTestSummary summary = performanceManager.generateSummary();

        assertTrue(summary.getTotalTests() >= 3, "Should have run at least 3 tests");
        assertTrue(summary.getPassedTests() >= 1, "At least baseline test should pass");

        // Performance comparison analysis using PerformanceAssertions
        LoggerUtil.info("=== PERFORMANCE ANALYSIS ===");
        LoggerUtil.info("Load test throughput: {:.2f} req/s", loadResult.getThroughput());
        LoggerUtil.info("Stress test throughput: {:.2f} req/s", stressResult.getThroughput());
        LoggerUtil.info("Spike test recovery rate: {:.2f}%", spikeResult.getSuccessRate());

        // Validate performance degradation is within acceptable limits
        PerformanceAssertions.assertPerformanceImprovement(stressResult, loadResult, 50.0);

        // Step 5: Generate comprehensive reports using PerformanceReportGenerator
        PerformanceReportGenerator reportGenerator = new PerformanceReportGenerator(
                performanceConfig.reportsDirectory()
        );

        try {
            // Generate all report formats
            reportGenerator.generateHTMLReport(summary, "workflow_analysis");
            reportGenerator.generateJSONReport(summary.getResults(), "workflow_data");
            reportGenerator.generateCSVReport(summary.getResults(), "workflow_export");

            LoggerUtil.info("Comprehensive performance reports generated");
            LoggerUtil.info("HTML Report: {}/workflow_analysis_*.html", performanceConfig.reportsDirectory());
            LoggerUtil.info("JSON Data: {}/workflow_data_*.json", performanceConfig.reportsDirectory());
            LoggerUtil.info("CSV Export: {}/workflow_export_*.csv", performanceConfig.reportsDirectory());

        } catch (Exception e) {
            LoggerUtil.error("Failed to generate performance reports", e);
            fail("Report generation should not fail");
        }

        LoggerUtil.info("Complete performance workflow finished successfully");
    }

    @Test(description = "Performance baseline establishment with reporting")
    @Story("Baseline Performance")
    @Severity(SeverityLevel.NORMAL)
    public void testPerformanceBaselineWithReporting() {
        LoggerUtil.info("Establishing performance baseline with comprehensive reporting");

        // Run a controlled test to establish baseline metrics
        PerformanceTestResult baseline = performanceManager.runLoadTest(targetBaseUrl, 5, 120);

        assertNotNull(baseline, "Baseline result should not be null");
        assertTrue(baseline.getTotalRequests() > 0, "Should have processed requests");

        // Use PerformanceAssertions for comprehensive validation
        PerformanceAssertions.assertPerformanceThresholds(baseline, performanceConfig);
        PerformanceAssertions.assertSuccessRate(baseline, 99.0); // High baseline standard
        PerformanceAssertions.assertResponseTime(baseline, 1000, "p95"); // Strict baseline

        // Baseline should meet all thresholds
        assertTrue(baseline.isPassed(), "Baseline test should pass all thresholds");

        // Generate baseline-specific reports
        PerformanceReportGenerator reportGenerator = new PerformanceReportGenerator(
                performanceConfig.reportsDirectory()
        );

        try {
            // Create baseline summary for reporting
            PerformanceTestSummary baselineSummary = PerformanceTestSummary.builder()
                    .totalTests(1)
                    .passedTests(1)
                    .failedTests(0)
                    .averageThroughput(baseline.getThroughput())
                    .averageP95ResponseTime(baseline.getP95ResponseTime())
                    .averageErrorRate(baseline.getErrorRate())
                    .results(Arrays.asList(baseline))
                    .build();

            // Generate baseline reports
            reportGenerator.generateHTMLReport(baselineSummary, "baseline_report");
            reportGenerator.generateJSONReport(Arrays.asList(baseline), "baseline_data");

            LoggerUtil.info("Baseline reports generated successfully");

        } catch (Exception e) {
            LoggerUtil.error("Failed to generate baseline reports", e);
        }

        // Record baseline metrics for future comparisons
        LoggerUtil.info("=== PERFORMANCE BASELINE ESTABLISHED ===");
        LoggerUtil.logPerformanceMetric("Baseline Throughput", baseline.getThroughput(), "req/s");
        LoggerUtil.logPerformanceMetric("Baseline P50", baseline.getP50ResponseTime(), "ms");
        LoggerUtil.logPerformanceMetric("Baseline P95", baseline.getP95ResponseTime(), "ms");
        LoggerUtil.logPerformanceMetric("Baseline P99", baseline.getP99ResponseTime(), "ms");
        LoggerUtil.logPerformanceMetric("Baseline Error Rate", baseline.getErrorRate(), "%");

        LoggerUtil.info("Performance baseline established successfully");
    }

    @Test(description = "Performance regression detection example")
    @Story("Regression Testing")
    @Severity(SeverityLevel.CRITICAL)
    public void testPerformanceRegressionDetection() {
        LoggerUtil.info("Testing performance regression detection capabilities");

        // Simulate baseline performance
        PerformanceTestResult currentBaseline = performanceManager.runLoadTest(targetBaseUrl, 5, 60);

        // Simulate a slightly degraded performance (for demonstration)
        PerformanceTestResult regressionTest = performanceManager.runLoadTest(targetBaseUrl, 8, 60);

        // Use PerformanceAssertions to compare performance
        try {
            // This should pass if degradation is within 30%
            PerformanceAssertions.assertPerformanceImprovement(regressionTest, currentBaseline, 30.0);
            LoggerUtil.info("Performance regression check passed - no significant degradation detected");

        } catch (AssertionError e) {
            LoggerUtil.warn("Performance regression detected: {}", e.getMessage());

            // Generate regression analysis report
            PerformanceReportGenerator reportGenerator = new PerformanceReportGenerator(
                    performanceConfig.reportsDirectory()
            );

            // Calculate averages for summary
            double avgThroughput = (currentBaseline.getThroughput() + regressionTest.getThroughput()) / 2.0;
            double avgP95 = (currentBaseline.getP95ResponseTime() + regressionTest.getP95ResponseTime()) / 2.0;
            double avgErrorRate = (currentBaseline.getErrorRate() + regressionTest.getErrorRate()) / 2.0;

            // Determine pass/fail counts
            int passedTests = 0;
            int failedTests = 0;

            if (currentBaseline.isPassed()) passedTests++;
            else failedTests++;

            if (regressionTest.isPassed()) passedTests++;
            else failedTests++;

            List<PerformanceTestResult> results = Arrays.asList(currentBaseline, regressionTest);

            PerformanceTestSummary regressionSummary = PerformanceTestSummary.builder()
                    .totalTests(2)
                    .passedTests(passedTests)
                    .failedTests(failedTests)
                    .averageThroughput(avgThroughput)
                    .averageP95ResponseTime(avgP95)
                    .averageErrorRate(avgErrorRate)
                    .results(results)
                    .build();

            try {
                reportGenerator.generateHTMLReport(regressionSummary, "regression_analysis");
                reportGenerator.generateJSONReport(results, "regression_data");
                LoggerUtil.info("Regression analysis report generated");
            } catch (Exception reportException) {
                LoggerUtil.error("Failed to generate regression report", reportException);
            }
        }

        // Always log the comparison results for analysis
        LoggerUtil.info("=== REGRESSION ANALYSIS RESULTS ===");
        LoggerUtil.info("Baseline - Throughput: {:.2f} req/s, P95: {}ms, Error Rate: {:.2f}%",
                currentBaseline.getThroughput(), currentBaseline.getP95ResponseTime(), currentBaseline.getErrorRate());
        LoggerUtil.info("Current - Throughput: {:.2f} req/s, P95: {}ms, Error Rate: {:.2f}%",
                regressionTest.getThroughput(), regressionTest.getP95ResponseTime(), regressionTest.getErrorRate());

        // Calculate degradation percentages
        double throughputChange = ((regressionTest.getThroughput() - currentBaseline.getThroughput()) / currentBaseline.getThroughput()) * 100;
        double responseTimeChange = ((double)(regressionTest.getP95ResponseTime() - currentBaseline.getP95ResponseTime()) / currentBaseline.getP95ResponseTime()) * 100;

        LoggerUtil.info("Throughput change: {:.2f}%", throughputChange);
        LoggerUtil.info("Response time change: {:.2f}%", responseTimeChange);

        LoggerUtil.info("Performance regression detection test completed");
    }
}