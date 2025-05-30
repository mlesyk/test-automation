package org.mlesyk.automation.performance;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class PerformanceTestSummary {
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private double averageThroughput;
    private double averageP95ResponseTime;
    private double averageErrorRate;
    private List<PerformanceTestResult> results;

    public double getPassRate() {
        if (totalTests == 0) return 0.0;
        return (double) passedTests / totalTests * 100;
    }
}
