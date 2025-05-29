package org.mlesyk.automation.tests;

import org.mlesyk.automation.base.BaseTest;
import org.mlesyk.automation.config.ConfigManager;
import org.mlesyk.automation.utils.LoggerUtil;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

@Epic("Framework Setup")
@Feature("Basic API Testing")
public class FrameworkSetupTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(FrameworkSetupTest.class);

    @Test(description = "Verify framework setup with simple GET request")
    @Story("Framework Verification")
    @Severity(SeverityLevel.BLOCKER)
    public void testFrameworkSetup() {
        LoggerUtil.logFrameworkInfo("Starting framework setup verification test");

        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/posts/1")
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", notNullValue())
                .body("body", notNullValue())
                .body("userId", notNullValue())
                .extract()
                .response();

        long responseTime = response.getTime();
        LoggerUtil.logApiRequest("GET", "/posts/1", response.getStatusCode(), responseTime);

        // Additional assertions with logging
        assertTrue(response.getTime() < config.responseTimeout() * 1000,
                "Response time should be less than configured timeout");
        LoggerUtil.logValidationPass("Response time check",
                config.responseTimeout() * 1000 + "ms",
                response.getTime() + "ms");

        String title = response.jsonPath().getString("title");
        assertNotNull(title, "Title should not be null");
        assertFalse(title.isEmpty(), "Title should not be empty");
        LoggerUtil.logValidationPass("Title validation", "not null/empty", title);

        // Performance logging
        LoggerUtil.logPerformanceMetric("Response Time", responseTime, "ms");

        LoggerUtil.info("Framework setup verified successfully!");
        LoggerUtil.info("Post title: {}", title);
    }

    @Test(description = "Verify configuration loading for different environments")
    @Story("Configuration Management")
    @Severity(SeverityLevel.CRITICAL)
    public void testConfigurationLoading() {
        LoggerUtil.logFrameworkInfo("Starting configuration loading verification");

        // Test configuration values with logging
        assertNotNull(config.baseUrl(), "Base URL should not be null");
        LoggerUtil.logValidationPass("Base URL check", "not null", config.baseUrl());

        assertTrue(config.requestTimeout() > 0, "Request timeout should be positive");
        LoggerUtil.logValidationPass("Request timeout check", "> 0", config.requestTimeout());

        assertTrue(config.responseTimeout() > 0, "Response timeout should be positive");
        LoggerUtil.logValidationPass("Response timeout check", "> 0", config.responseTimeout());

        assertTrue(config.retryCount() >= 0, "Retry count should be non-negative");
        LoggerUtil.logValidationPass("Retry count check", ">= 0", config.retryCount());

        LoggerUtil.info("Configuration validation completed successfully");
        LoggerUtil.info("Environment: {}", ConfigManager.getEnvironment());
        LoggerUtil.info("Base URL: {}", config.baseUrl());
        LoggerUtil.info("Request Timeout: {}s", config.requestTimeout());
        LoggerUtil.info("Response Timeout: {}s", config.responseTimeout());
    }

    @Test(description = "Test multiple HTTP methods")
    @Story("HTTP Methods Support")
    @Severity(SeverityLevel.NORMAL)
    public void testHttpMethods() {
        LoggerUtil.logFrameworkInfo("Starting HTTP methods support verification");

        // GET request with logging
        LoggerUtil.debug("Testing GET request...");

        Response getResponse = given()
                .spec(requestSpec)
                .when()
                .get("/posts/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .extract()
                .response();

        LoggerUtil.logApiRequest("GET", "/posts/1", getResponse.getStatusCode(), getResponse.getTime());

        // POST request with logging
        LoggerUtil.debug("Testing POST request...");
        String newPost = """
                {
                    "title": "Test Post",
                    "body": "This is a test post body",
                    "userId": 1
                }
                """;

        Response postResponse = given()
                .spec(requestSpec)
                .body(newPost)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .body("title", equalTo("Test Post"))
                .body("body", equalTo("This is a test post body"))
                .body("userId", equalTo(1))
                .extract()
                .response();

        LoggerUtil.logApiRequest("POST", "/posts", postResponse.getStatusCode(), postResponse.getTime());

        int createdPostId = postResponse.jsonPath().getInt("id");
        assertTrue(createdPostId > 0, "Created post should have valid ID");
        LoggerUtil.logValidationPass("Created post ID", "> 0", createdPostId);

        // Performance comparison
        LoggerUtil.logPerformanceMetric("GET Response Time", getResponse.getTime(), "ms");
        LoggerUtil.logPerformanceMetric("POST Response Time", postResponse.getTime(), "ms");

        LoggerUtil.info("HTTP methods test completed successfully");
        LoggerUtil.info("Created post ID: {}", createdPostId);
    }
}
