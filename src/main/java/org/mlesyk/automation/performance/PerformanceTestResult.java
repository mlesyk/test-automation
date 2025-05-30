package org.mlesyk.automation.performance;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class PerformanceTestResult {
    private String testName;
    private String testType; // LOAD, STRESS, SPIKE, VOLUME
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationSeconds;

    // Core metrics
    private long totalRequests;
    private long successfulRequests;
    private long failedRequests;
    private double errorRate;
    private double throughput; // requests per second

    // Response time metrics
    private long averageResponseTime;
    private long minResponseTime;
    private long maxResponseTime;
    private long p50ResponseTime;
    private long p95ResponseTime;
    private long p99ResponseTime;

    // Additional metrics
    private Map<String, Object> customMetrics;
    private String reportPath;
    private boolean passed;
    private String failureReason;

    public double getSuccessRate() {
        if (totalRequests == 0) return 0.0;
        return (double) successfulRequests / totalRequests * 100;
    }

    public boolean isWithinThresholds(PerformanceConfig config) {
        return errorRate <= config.errorRateThreshold() &&
                p95ResponseTime <= config.responseTimeP95Threshold() &&
                p99ResponseTime <= config.responseTimeP99Threshold() &&
                throughput >= config.minimumThroughput();
    }
}
