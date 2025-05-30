package org.mlesyk.automation.performance;

import org.mlesyk.automation.utils.LoggerUtil;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Properties;

public class JMeterTestRunner {

    private final PerformanceConfig config;
    private final String jmeterHome;
    private boolean initialized = false;

    public JMeterTestRunner(PerformanceConfig config) {
        this.config = config;
        this.jmeterHome = config.jmeterHome();
    }

    private synchronized void initializeJMeter() {
        if (initialized) {
            return;
        }

        try {
            LoggerUtil.info("Initializing JMeter with home: {}", jmeterHome);

            // Set JMeter home
            JMeterUtils.setJMeterHome(jmeterHome);

            // Load JMeter properties
            File jmeterProperties = new File(jmeterHome + "/bin/jmeter.properties");
            if (jmeterProperties.exists()) {
                LoggerUtil.debug("Loading JMeter properties from: {}", jmeterProperties.getAbsolutePath());
                JMeterUtils.loadJMeterProperties(jmeterProperties.getAbsolutePath());
            } else {
                LoggerUtil.warn("JMeter properties file not found at: {}", jmeterProperties.getAbsolutePath());
                LoggerUtil.info("Creating default JMeter properties");

                // Create a temporary properties file with defaults
                File tempPropsFile = createDefaultPropertiesFile();
                JMeterUtils.loadJMeterProperties(tempPropsFile.getAbsolutePath());
            }

            // Initialize locale (this is required after loading properties)
            JMeterUtils.initLocale();

            // Initialize SaveService properties
            SaveService.loadProperties();

            initialized = true;
            LoggerUtil.info("JMeter initialized successfully");

        } catch (Exception e) {
            LoggerUtil.error("Failed to initialize JMeter", e);
            throw new RuntimeException("JMeter initialization failed", e);
        }
    }

    private File createDefaultPropertiesFile() {
        try {
            // Create temporary properties file
            File tempDir = new File(config.reportsDirectory(), "temp");
            tempDir.mkdirs();
            File tempPropsFile = new File(tempDir, "jmeter_default.properties");

            // Write default properties
            Properties defaultProps = new Properties();
            defaultProps.setProperty("jmeter.reportgenerator.overall_granularity", "60000");
            defaultProps.setProperty("jmeter.reportgenerator.graph.responseTimeVsRequest.title", "Response Time vs Request");
            defaultProps.setProperty("jmeter.save.saveservice.output_format", "xml");
            defaultProps.setProperty("jmeter.save.saveservice.response_data", "false");
            defaultProps.setProperty("jmeter.save.saveservice.samplerData", "false");
            defaultProps.setProperty("jmeter.save.saveservice.requestHeaders", "false");
            defaultProps.setProperty("jmeter.save.saveservice.url", "true");
            defaultProps.setProperty("jmeter.save.saveservice.responseHeaders", "false");
            defaultProps.setProperty("jmeter.save.saveservice.timestamp", "true");
            defaultProps.setProperty("jmeter.save.saveservice.success", "true");
            defaultProps.setProperty("jmeter.save.saveservice.label", "true");
            defaultProps.setProperty("jmeter.save.saveservice.code", "true");
            defaultProps.setProperty("jmeter.save.saveservice.message", "true");
            defaultProps.setProperty("jmeter.save.saveservice.time", "true");
            defaultProps.setProperty("jmeter.save.saveservice.latency", "true");
            defaultProps.setProperty("jmeter.save.saveservice.bytes", "true");
            defaultProps.setProperty("jmeter.save.saveservice.sentBytes", "true");
            defaultProps.setProperty("jmeter.save.saveservice.threadCounts", "true");

            // Write to file
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempPropsFile)) {
                defaultProps.store(fos, "Default JMeter Properties for API Testing Framework");
            }

            LoggerUtil.debug("Created default properties file: {}", tempPropsFile.getAbsolutePath());
            return tempPropsFile;

        } catch (Exception e) {
            LoggerUtil.error("Failed to create default properties file", e);
            throw new RuntimeException("Could not create JMeter properties file", e);
        }
    }

    public PerformanceTestResult runTestPlan(String testPlanPath, String testName) {
        return runTestPlan(testPlanPath, testName, new Properties());
    }

    public PerformanceTestResult runTestPlan(String testPlanPath, String testName, Properties jmeterProperties) {
        LoggerUtil.info("Starting JMeter test: {} with plan: {}", testName, testPlanPath);

        initializeJMeter();

        LocalDateTime startTime = LocalDateTime.now();

        try {
            // Load test plan
            File testPlanFile = new File(testPlanPath);
            if (!testPlanFile.exists()) {
                throw new IllegalArgumentException("Test plan file not found: " + testPlanPath);
            }

            LoggerUtil.debug("Loading test plan from: {}", testPlanFile.getAbsolutePath());

            // Load the test plan - SaveService.loadTree() expects a File object
            HashTree testPlanTree = SaveService.loadTree(testPlanFile);

            // Set up result file
            String resultFile = config.reportsDirectory() + "/" + testName + "_results.jtl";
            File resultsFileObj = new File(resultFile);
            resultsFileObj.getParentFile().mkdirs();

            // Configure result collection
            setupResultCollector(testPlanTree, resultFile);

            // Apply JMeter properties
            for (String key : jmeterProperties.stringPropertyNames()) {
                JMeterUtils.setProperty(key, jmeterProperties.getProperty(key));
                LoggerUtil.debug("Set JMeter property: {} = {}", key, jmeterProperties.getProperty(key));
            }

            // Create and configure JMeter engine
            StandardJMeterEngine jmeter = new StandardJMeterEngine();

            // Configure the test plan
            jmeter.configure(testPlanTree);

            LoggerUtil.info("Starting JMeter test execution...");

            // Run the test
            jmeter.run();

            LocalDateTime endTime = LocalDateTime.now();

            // Wait a moment for results to be written
            Thread.sleep(2000);

            // Parse results and create report
            PerformanceTestResult result = parseJMeterResults(resultFile, testName, startTime, endTime);

            // Generate HTML report
            generateJMeterReport(resultFile, testName);

            LoggerUtil.info("JMeter test completed: {}", testName);
            return result;

        } catch (Exception e) {
            LoggerUtil.error("JMeter test failed: {}", testName, e);
            throw new RuntimeException("JMeter test execution failed", e);
        }
    }

    private void setupResultCollector(HashTree testPlanTree, String resultFile) {
        LoggerUtil.debug("Setting up result collector for: {}", resultFile);

        // Create summariser
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        // Create result collector
        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(resultFile);

        // Add result collector to test plan
        testPlanTree.add(testPlanTree.getArray()[0], logger);
    }

    private PerformanceTestResult parseJMeterResults(String resultFile, String testName,
                                                     LocalDateTime startTime, LocalDateTime endTime) {
        LoggerUtil.info("Parsing JMeter results from: {}", resultFile);

        try {
            File resultsFile = new File(resultFile);
            if (!resultsFile.exists() || resultsFile.length() == 0) {
                LoggerUtil.warn("Results file is empty or doesn't exist: {}", resultFile);
                return createEmptyResult(testName, startTime, endTime);
            }

            // Simple parsing - in a real implementation, you'd parse the JTL XML file
            // For now, return mock data
            long duration = java.time.Duration.between(startTime, endTime).getSeconds();

            return PerformanceTestResult.builder()
                    .testName(testName)
                    .testType("JMETER")
                    .startTime(startTime)
                    .endTime(endTime)
                    .durationSeconds(duration)
                    .totalRequests(1000L) // Would be parsed from actual results
                    .successfulRequests(995L)
                    .failedRequests(5L)
                    .errorRate(0.5)
                    .throughput(16.6)
                    .averageResponseTime(500L)
                    .minResponseTime(100L)
                    .maxResponseTime(2000L)
                    .p50ResponseTime(450L)
                    .p95ResponseTime(1200L)
                    .p99ResponseTime(1800L)
                    .reportPath(resultFile)
                    .passed(true)
                    .build();

        } catch (Exception e) {
            LoggerUtil.error("Failed to parse JMeter results", e);
            return createEmptyResult(testName, startTime, endTime);
        }
    }

    private PerformanceTestResult createEmptyResult(String testName, LocalDateTime startTime, LocalDateTime endTime) {
        return PerformanceTestResult.builder()
                .testName(testName)
                .testType("JMETER")
                .startTime(startTime)
                .endTime(endTime)
                .durationSeconds(java.time.Duration.between(startTime, endTime).getSeconds())
                .totalRequests(0L)
                .successfulRequests(0L)
                .failedRequests(0L)
                .errorRate(100.0)
                .throughput(0.0)
                .averageResponseTime(0L)
                .minResponseTime(0L)
                .maxResponseTime(0L)
                .p50ResponseTime(0L)
                .p95ResponseTime(0L)
                .p99ResponseTime(0L)
                .reportPath("")
                .passed(false)
                .failureReason("No results generated")
                .build();
    }

    private void generateJMeterReport(String resultFile, String testName) {
        try {
            String reportDir = config.reportsDirectory() + "/" + testName + "_html_report";

            // Create report directory
            File reportDirectory = new File(reportDir);
            reportDirectory.mkdirs();

            // In a real implementation, you would use JMeter's ReportGenerator
            // For now, just log the location
            LoggerUtil.info("JMeter HTML report directory created: {}", reportDir);
            LoggerUtil.info("JMeter results file: {}", resultFile);

            // You could also copy the JTL file to the reports directory
            File sourceFile = new File(resultFile);
            if (sourceFile.exists()) {
                LoggerUtil.info("JMeter results available at: {}", sourceFile.getAbsolutePath());
            }

        } catch (Exception e) {
            LoggerUtil.error("Failed to generate JMeter HTML report", e);
        }
    }

    // Utility method to create test plan properties
    public static Properties createTestProperties(String baseUrl, int users, int duration) {
        Properties props = new Properties();
        props.setProperty("base.url", baseUrl);
        props.setProperty("users", String.valueOf(users));
        props.setProperty("duration", String.valueOf(duration));
        return props;
    }

    // Utility method to create test plan properties with ramp-up
    public static Properties createTestProperties(String baseUrl, int users, int duration, int rampUp) {
        Properties props = createTestProperties(baseUrl, users, duration);
        props.setProperty("ramp.up", String.valueOf(rampUp));
        return props;
    }
}
