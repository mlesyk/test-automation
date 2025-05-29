package org.mlesyk.automation.base;

import org.mlesyk.automation.config.ConfigManager;
import org.mlesyk.automation.config.Configuration;
import org.mlesyk.automation.utils.LoggerUtil;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    protected Configuration config;
    protected RequestSpecification requestSpec;
    protected ResponseSpecification responseSpec;
    private long testStartTime;

    @BeforeClass
    public void setUpClass() {
        LoggerUtil.logFrameworkInfo("Initializing test framework...");
        config = ConfigManager.getConfig();
        setupRestAssured();
        logConfigurationInfo();
        LoggerUtil.logFrameworkInfo("Test framework initialization completed");
    }

    @BeforeMethod
    public void setUp(Method method) {
        testStartTime = System.currentTimeMillis();
        String testName = method.getName();
        String testClass = method.getDeclaringClass().getSimpleName();

        LoggerUtil.setTestContext(testName, testClass);

        // Get test description from TestNG annotations if available
        String description = getTestDescription(method);
        LoggerUtil.logTestStart(testName, description);
    }

    @AfterMethod
    public void tearDown(Method method) {
        long executionTime = System.currentTimeMillis() - testStartTime;
        String testName = method.getName();

        // This will be overridden by TestNG listeners for actual results
        LoggerUtil.logTestPass(testName, executionTime);
        LoggerUtil.clearTestContext();
    }

    private void setupRestAssured() {
        LoggerUtil.logFrameworkInfo("Configuring RestAssured...");

        // Base URI configuration
        RestAssured.baseURI = config.baseUrl();
        LoggerUtil.debug("Base URI set to: {}", config.baseUrl());

        // Request specification
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("User-Agent", "API-Testing-Framework/1.0")
                .addFilter(new AllureRestAssured()) // For Allure reporting
                .build();

        // Response specification
        responseSpec = new ResponseSpecBuilder()
                .expectResponseTime(org.hamcrest.Matchers.lessThan(TimeUnit.SECONDS.toMillis(config.responseTimeout())))
                .build();

        // Global settings
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        LoggerUtil.logFrameworkInfo("RestAssured configuration completed");
    }

    private void logConfigurationInfo() {
        LoggerUtil.logConfigInfo(
                ConfigManager.getEnvironment(),
                config.baseUrl(),
                config.responseTimeout()
        );

        LoggerUtil.debug("Additional configuration:");
        LoggerUtil.debug("  Request timeout: {}s", config.requestTimeout());
        LoggerUtil.debug("  Retry count: {}", config.retryCount());
        LoggerUtil.debug("  Parallel threads: {}", config.parallelThreads());
    }

    private String getTestDescription(Method method) {
        // Try to get description from TestNG @Test annotation
        org.testng.annotations.Test testAnnotation = method.getAnnotation(org.testng.annotations.Test.class);
        if (testAnnotation != null && !testAnnotation.description().isEmpty()) {
            return testAnnotation.description();
        }
        return "No description provided";
    }

    // Utility method for logging test information (keeping for backward compatibility)
    protected void logTestInfo(String testName, String description) {
        LoggerUtil.logTestStart(testName, description);
    }
}