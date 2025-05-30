package org.mlesyk.automation.tests.performance;

import org.mlesyk.automation.base.BaseTest;
import org.mlesyk.automation.performance.PerformanceReportGenerator;
import org.mlesyk.automation.performance.PerformanceTestManager;
import org.mlesyk.automation.performance.PerformanceConfig;
import org.mlesyk.automation.performance.PerformanceTestSummary;
import org.mlesyk.automation.utils.LoggerUtil;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;

public class PerformanceBaseTest extends BaseTest {

    protected PerformanceTestManager performanceManager;
    protected PerformanceConfig performanceConfig;
    protected String targetBaseUrl;

    @BeforeClass
    public void setUpPerformance() {
        super.setUpClass();
        performanceConfig = ConfigFactory.create(PerformanceConfig.class);
        performanceManager = new PerformanceTestManager();
        targetBaseUrl = config.baseUrl();

        LoggerUtil.logFrameworkInfo("Performance testing framework initialized");
        LoggerUtil.info("Target URL: {}", targetBaseUrl);
        LoggerUtil.info("Load test users: {}", performanceConfig.loadTestUsers());
        LoggerUtil.info("P95 threshold: {}ms", performanceConfig.responseTimeP95Threshold());
        LoggerUtil.info("Error rate threshold: {}%", performanceConfig.errorRateThreshold());
    }

    @AfterClass
    public void tearDownPerformance() {
        if (performanceManager != null) {
            PerformanceTestSummary summary = performanceManager.generateSummary();

            LoggerUtil.info("=== PERFORMANCE TEST SUMMARY ===");
            LoggerUtil.info("Total tests: {}", summary.getTotalTests());
            LoggerUtil.info("Passed tests: {}", summary.getPassedTests());
            LoggerUtil.info("Failed tests: {}", summary.getFailedTests());
            LoggerUtil.info("Pass rate: {:.1f}%", summary.getPassRate());
            LoggerUtil.info("Average throughput: {:.2f} req/s", summary.getAverageThroughput());
            LoggerUtil.info("Average P95 response time: {:.0f}ms", summary.getAverageP95ResponseTime());
            LoggerUtil.info("Average error rate: {:.2f}%", summary.getAverageErrorRate());

            LoggerUtil.logPerformanceMetric("Final Pass Rate", summary.getPassRate(), "%");
            LoggerUtil.logPerformanceMetric("Final Average Throughput", summary.getAverageThroughput(), "req/s");

            // Generate comprehensive reports using PerformanceReportGenerator
            PerformanceReportGenerator reportGenerator = new PerformanceReportGenerator(
                    performanceConfig.reportsDirectory()
            );

            try {
                // Generate all report formats
                reportGenerator.generateHTMLReport(summary, "performance_summary");
                reportGenerator.generateJSONReport(summary.getResults(), "performance_data");
                reportGenerator.generateCSVReport(summary.getResults(), "performance_export");

                LoggerUtil.info("All performance reports generated successfully");
                LoggerUtil.info("Reports location: {}", performanceConfig.reportsDirectory());

            } catch (Exception e) {
                LoggerUtil.error("Failed to generate performance reports", e);
            }
        }
    }
}
