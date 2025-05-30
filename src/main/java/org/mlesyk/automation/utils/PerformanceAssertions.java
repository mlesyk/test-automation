package org.mlesyk.automation.utils;

import org.mlesyk.automation.performance.PerformanceTestResult;
import org.mlesyk.automation.performance.PerformanceConfig;

import static org.testng.Assert.*;

public class PerformanceAssertions {

    public static void assertResponseTime(PerformanceTestResult result, long maxResponseTimeMs, String percentile) {
        long actualResponseTime;

        switch (percentile.toLowerCase()) {
            case "avg":
            case "average":
                actualResponseTime = result.getAverageResponseTime();
                break;
            case "p50":
            case "median":
                actualResponseTime = result.getP50ResponseTime();
                break;
            case "p95":
                actualResponseTime = result.getP95ResponseTime();
                break;
            case "p99":
                actualResponseTime = result.getP99ResponseTime();
                break;
            case "max":
                actualResponseTime = result.getMaxResponseTime();
                break;
            default:
                throw new IllegalArgumentException("Unsupported percentile: " + percentile);
        }

        assertTrue(actualResponseTime <= maxResponseTimeMs,
                String.format("%s response time %dms should be <= %dms",
                        percentile, actualResponseTime, maxResponseTimeMs));

        LoggerUtil.logValidationPass(percentile + " response time",
                "<= " + maxResponseTimeMs + "ms",
                actualResponseTime + "ms");
    }

    public static void assertThroughput(PerformanceTestResult result, double minThroughput) {
        assertTrue(result.getThroughput() >= minThroughput,
                String.format("Throughput %.2f req/s should be >= %.2f req/s",
                        result.getThroughput(), minThroughput));

        LoggerUtil.logValidationPass("Throughput",
                ">= " + minThroughput + " req/s",
                result.getThroughput() + " req/s");
    }

    public static void assertErrorRate(PerformanceTestResult result, double maxErrorRate) {
        assertTrue(result.getErrorRate() <= maxErrorRate,
                String.format("Error rate %.2f%% should be <= %.2f%%",
                        result.getErrorRate(), maxErrorRate));

        LoggerUtil.logValidationPass("Error rate",
                "<= " + maxErrorRate + "%",
                result.getErrorRate() + "%");
    }

    public static void assertSuccessRate(PerformanceTestResult result, double minSuccessRate) {
        double actualSuccessRate = result.getSuccessRate();
        assertTrue(actualSuccessRate >= minSuccessRate,
                String.format("Success rate %.2f%% should be >= %.2f%%",
                        actualSuccessRate, minSuccessRate));

        LoggerUtil.logValidationPass("Success rate",
                ">= " + minSuccessRate + "%",
                actualSuccessRate + "%");
    }

    public static void assertPerformanceThresholds(PerformanceTestResult result, PerformanceConfig config) {
        assertResponseTime(result, config.responseTimeP95Threshold(), "p95");
        assertResponseTime(result, config.responseTimeP99Threshold(), "p99");
        assertErrorRate(result, config.errorRateThreshold());
        assertThroughput(result, config.minimumThroughput());

        LoggerUtil.info("All performance thresholds validated successfully");
    }

    public static void assertPerformanceImprovement(PerformanceTestResult current, PerformanceTestResult baseline,
                                                    double maxDegradationPercent) {
        // Check throughput degradation
        double throughputChange = (current.getThroughput() - baseline.getThroughput()) / baseline.getThroughput() * 100;
        assertTrue(throughputChange >= -maxDegradationPercent,
                String.format("Throughput degradation %.2f%% should not exceed %.2f%%",
                        -throughputChange, maxDegradationPercent));

        // Check response time degradation
        double responseTimeChange = (double)(current.getP95ResponseTime() - baseline.getP95ResponseTime()) / baseline.getP95ResponseTime() * 100;
        assertTrue(responseTimeChange <= maxDegradationPercent,
                String.format("P95 response time degradation %.2f%% should not exceed %.2f%%",
                        responseTimeChange, maxDegradationPercent));

        LoggerUtil.logValidationPass("Performance comparison",
                "within " + maxDegradationPercent + "% degradation",
                "validated");
    }
}