package org.mlesyk.automation.performance;

import org.aeonbits.owner.Config;

@Config.Sources({
        "classpath:config/performance.properties"
})
public interface PerformanceConfig extends Config {

    @Key("performance.load.users")
    @DefaultValue("10")
    int loadTestUsers();

    @Key("performance.load.duration")
    @DefaultValue("60")
    int loadTestDurationSeconds();

    @Key("performance.stress.users")
    @DefaultValue("50")
    int stressTestUsers();

    @Key("performance.stress.duration")
    @DefaultValue("120")
    int stressTestDurationSeconds();

    @Key("performance.spike.users")
    @DefaultValue("100")
    int spikeTestUsers();

    @Key("performance.spike.duration")
    @DefaultValue("30")
    int spikeTestDurationSeconds();

    @Key("performance.response.threshold.p95")
    @DefaultValue("2000")
    int responseTimeP95Threshold();

    @Key("performance.response.threshold.p99")
    @DefaultValue("5000")
    int responseTimeP99Threshold();

    @Key("performance.error.rate.threshold")
    @DefaultValue("1.0")
    double errorRateThreshold();

    @Key("performance.throughput.min")
    @DefaultValue("10")
    double minimumThroughput();

    @Key("jmeter.home")
    @DefaultValue("/usr/local/jmeter")
    String jmeterHome();

    @Key("k6.binary")
    @DefaultValue("k6")
    String k6Binary();

    @Key("performance.reports.dir")
    @DefaultValue("target/performance-reports")
    String reportsDirectory();
}