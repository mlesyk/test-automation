package org.mlesyk.automation.services;

import org.mlesyk.automation.config.ConfigManager;
import org.mlesyk.automation.config.Configuration;
import org.mlesyk.automation.utils.LoggerUtil;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public abstract class BaseService {

    protected Configuration config;
    protected RequestSpecification requestSpec;
    protected ResponseSpecification responseSpec;

    public BaseService(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this.config = ConfigManager.getConfig();
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    // Common HTTP methods with logging
    protected Response performGet(String endpoint) {
        LoggerUtil.debug("Performing GET request to: {}", endpoint);
        long startTime = System.currentTimeMillis();

        Response response = given()
                .spec(requestSpec)
                .when()
                .get(endpoint)
                .then()
                .spec(responseSpec)
                .extract()
                .response();

        long responseTime = response.getTime();
        LoggerUtil.logApiRequest("GET", endpoint, response.getStatusCode(), responseTime);

        return response;
    }

    protected Response performPost(String endpoint, Object body) {
        LoggerUtil.debug("Performing POST request to: {} with body: {}", endpoint, body);
        long startTime = System.currentTimeMillis();

        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .spec(responseSpec)
                .extract()
                .response();

        long responseTime = response.getTime();
        LoggerUtil.logApiRequest("POST", endpoint, response.getStatusCode(), responseTime);

        return response;
    }

    protected Response performPut(String endpoint, Object body) {
        LoggerUtil.debug("Performing PUT request to: {} with body: {}", endpoint, body);

        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .put(endpoint)
                .then()
                .spec(responseSpec)
                .extract()
                .response();

        LoggerUtil.logApiRequest("PUT", endpoint, response.getStatusCode(), response.getTime());
        return response;
    }

    protected Response performPatch(String endpoint, Object body) {
        LoggerUtil.debug("Performing PATCH request to: {} with body: {}", endpoint, body);

        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .patch(endpoint)
                .then()
                .spec(responseSpec)
                .extract()
                .response();

        LoggerUtil.logApiRequest("PATCH", endpoint, response.getStatusCode(), response.getTime());
        return response;
    }

    protected Response performDelete(String endpoint) {
        LoggerUtil.debug("Performing DELETE request to: {}", endpoint);

        Response response = given()
                .spec(requestSpec)
                .when()
                .delete(endpoint)
                .then()
                .spec(responseSpec)
                .extract()
                .response();

        LoggerUtil.logApiRequest("DELETE", endpoint, response.getStatusCode(), response.getTime());
        return response;
    }

    // Generic request with custom specifications
    protected Response performRequest(String method, String endpoint, Object body,
                                      RequestSpecification customRequestSpec) {
        LoggerUtil.debug("Performing {} request to: {}", method, endpoint);

        RequestSpecification spec = customRequestSpec != null ? customRequestSpec : requestSpec;

        Response response = given()
                .spec(spec)
                .body(body)
                .when()
                .request(method, endpoint)
                .then()
                .extract()
                .response();

        LoggerUtil.logApiRequest(method.toUpperCase(), endpoint, response.getStatusCode(), response.getTime());
        return response;
    }
}
