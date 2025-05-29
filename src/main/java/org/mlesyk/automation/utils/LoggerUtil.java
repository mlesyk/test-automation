package org.mlesyk.automation.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LoggerUtil {

    private static final Logger logger = LoggerFactory.getLogger(LoggerUtil.class);
    private static final Logger testResultsLogger = LoggerFactory.getLogger("TEST_RESULTS");

    // Test execution context
    public static void setTestContext(String testName, String testClass) {
        MDC.put("testName", testName);
        MDC.put("testClass", testClass);
    }

    public static void clearTestContext() {
        MDC.clear();
    }

    // Test lifecycle logging
    public static void logTestStart(String testName, String description) {
        testResultsLogger.info("STARTING TEST: {} - {}", testName, description);
        logger.info("Test started: {} - {}", testName, description);
    }

    public static void logTestPass(String testName, long executionTime) {
        testResultsLogger.info("PASSED: {} ({}ms)", testName, executionTime);
        logger.info("Test passed: {} in {}ms", testName, executionTime);
    }

    public static void logTestFail(String testName, String reason, long executionTime) {
        testResultsLogger.error("FAILED: {} - {} ({}ms)", testName, reason, executionTime);
        logger.error("Test failed: {} - {} in {}ms", testName, reason, executionTime);
    }

    public static void logTestSkip(String testName, String reason) {
        testResultsLogger.warn("SKIPPED: {} - {}", testName, reason);
        logger.warn("Test skipped: {} - {}", testName, reason);
    }

    // Configuration logging
    public static void logConfigInfo(String environment, String baseUrl, int timeout) {
        logger.info("Configuration loaded - Environment: {}, Base URL: {}, Timeout: {}s",
                environment, baseUrl, timeout);
        testResultsLogger.info("Environment: {} | Base URL: {} | Timeout: {}s",
                environment, baseUrl, timeout);
    }

    // API request/response logging
    public static void logApiRequest(String method, String endpoint, int statusCode, long responseTime) {
        logger.debug("API {} {} -> {} ({}ms)", method, endpoint, statusCode, responseTime);
        if (responseTime > 2000) { // Log slow requests
            logger.warn("Slow API response: {} {} took {}ms", method, endpoint, responseTime);
        }
    }

    public static void logApiError(String method, String endpoint, int statusCode, String error) {
        logger.error("API Error: {} {} -> {} - {}", method, endpoint, statusCode, error);
        testResultsLogger.error("API Error: {} {} -> {} - {}", method, endpoint, statusCode, error);
    }

    // Framework logging
    public static void logFrameworkInfo(String message, Object... args) {
        logger.info(message, args);
    }

    public static void logFrameworkError(String message, Throwable throwable) {
        logger.error("Framework Error: {}", message, throwable);
    }

    // Validation logging
    public static void logValidationPass(String validation, Object expected, Object actual) {
        logger.debug("Validation passed: {} - Expected: {}, Actual: {}", validation, expected, actual);
    }

    public static void logValidationFail(String validation, Object expected, Object actual) {
        logger.error("Validation failed: {} - Expected: {}, Actual: {}", validation, expected, actual);
        testResultsLogger.error("Validation failed: {} - Expected: {}, Actual: {}", validation, expected, actual);
    }

    // Performance logging
    public static void logPerformanceMetric(String metric, Object value, String unit) {
        logger.info("Performance: {} = {} {}", metric, value, unit);
        testResultsLogger.info("Performance: {} = {} {}", metric, value, unit);
    }

    // Authentication logging
    public static void logAuthSuccess(String authType, String user) {
        logger.info("Authentication successful: {} for user: {}", authType, user);
    }

    public static void logAuthFailure(String authType, String reason) {
        logger.error("Authentication failed: {} - {}", authType, reason);
        testResultsLogger.error("Authentication failed: {} - {}", authType, reason);
    }

    // Generic logging methods
    public static void info(String message, Object... args) {
        logger.info(message, args);
    }

    public static void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    public static void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public static void error(String message, Object... args) {
        logger.error(message, args);
    }

    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}
