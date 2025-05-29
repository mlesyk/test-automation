package org.mlesyk.automation.tests;

import org.mlesyk.automation.base.BaseTest;
import org.mlesyk.automation.models.User;
import org.mlesyk.automation.services.UserService;
import org.mlesyk.automation.utils.LoggerUtil;
import org.mlesyk.automation.utils.TestDataUtil;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

@Epic("User Management")
@Feature("User Service")
public class UserServiceTest extends BaseTest {

    private UserService userService;

    @BeforeClass
    public void setUpUserService() {
        userService = new UserService(requestSpec, responseSpec);
        LoggerUtil.logFrameworkInfo("UserService initialized for testing");
    }

    @Test(description = "Verify getting all users")
    @Story("Get Users")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetAllUsers() {
        LoggerUtil.info("Testing get all users functionality");

        // Using service method - returns Response
        Response response = userService.getAllUsers();

        // Basic response validations
        assertEquals(response.getStatusCode(), 200, "Status code should be 200");
        LoggerUtil.logValidationPass("Status code check", 200, response.getStatusCode());

        // Validate response structure
        response.then()
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue())
                .body("[0].name", notNullValue())
                .body("[0].email", notNullValue());

        // Using service method - returns objects
        List<User> users = userService.getAllUsersAsObjects();
        assertFalse(users.isEmpty(), "Users list should not be empty");
        LoggerUtil.logValidationPass("Users list size", "> 0", users.size());

        // Validate first user object
        User firstUser = users.get(0);
        assertNotNull(firstUser.getId(), "User ID should not be null");
        assertNotNull(firstUser.getName(), "User name should not be null");
        assertNotNull(firstUser.getEmail(), "User email should not be null");

        LoggerUtil.info("Successfully retrieved {} users", users.size());
    }

    @Test(description = "Verify getting user by ID")
    @Story("Get User by ID")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetUserById() {
        int userId = 1;
        LoggerUtil.info("Testing get user by ID: {}", userId);

        // Using service method
        Response response = userService.getUserById(userId);

        assertEquals(response.getStatusCode(), 200, "Status code should be 200");

        // Validate specific user data
        response.then()
                .body("id", equalTo(userId))
                .body("name", notNullValue())
                .body("username", notNullValue())
                .body("email", notNullValue())
                .body("address.city", notNullValue())
                .body("company.name", notNullValue());

        // Using object method
        User user = userService.getUserByIdAsObject(userId);
        assertEquals(user.getId(), Integer.valueOf(userId), "User ID should match requested ID");
        assertNotNull(user.getAddress(), "User address should not be null");
        assertNotNull(user.getCompany(), "User company should not be null");

        LoggerUtil.info("Successfully retrieved user: {}", user.getName());
    }

    @Test(description = "Verify creating a new user")
    @Story("Create User")
    @Severity(SeverityLevel.NORMAL)
    public void testCreateUser() {
        LoggerUtil.info("Testing create user functionality");

        // Generate test data using TestDataUtil
        Map<String, Object> userData = TestDataUtil.generateUserData();

        // Create User object from test data
        User newUser = new User();
        newUser.setName((String) userData.get("name"));
        newUser.setUsername((String) userData.get("username"));
        newUser.setEmail((String) userData.get("email"));
        newUser.setPhone((String) userData.get("phone"));
        newUser.setWebsite((String) userData.get("website"));

        // Using service to create user
        Response response = userService.createUser(newUser);

        assertEquals(response.getStatusCode(), 201, "Status code should be 201 for creation");
        LoggerUtil.logValidationPass("Create user status", 201, response.getStatusCode());

        // Validate created user response
        response.then()
                .body("name", equalTo(newUser.getName()))
                .body("username", equalTo(newUser.getUsername()))
                .body("email", equalTo(newUser.getEmail()))
                .body("id", notNullValue());

        // Using object method
        User createdUser = userService.createUserAndReturn(newUser);
        assertNotNull(createdUser.getId(), "Created user should have an ID");
        assertEquals(createdUser.getName(), newUser.getName(), "Name should match");

        LoggerUtil.info("Successfully created user with ID: {}", createdUser.getId());
    }

    @Test(description = "Verify updating an existing user")
    @Story("Update User")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdateUser() {
        int userId = 1;
        LoggerUtil.info("Testing update user functionality for ID: {}", userId);

        // Get existing user first
        User existingUser = userService.getUserByIdAsObject(userId);
        LoggerUtil.info("Retrieved existing user: {}", existingUser.getName());

        // Modify user data
        existingUser.setName("Updated " + existingUser.getName());
        existingUser.setEmail("updated." + existingUser.getEmail());

        // Update user using service
        Response response = userService.updateUser(userId, existingUser);

        assertEquals(response.getStatusCode(), 200, "Status code should be 200 for update");

        // Validate updated response
        response.then()
                .body("id", equalTo(userId))
                .body("name", equalTo(existingUser.getName()))
                .body("email", equalTo(existingUser.getEmail()));

        LoggerUtil.info("Successfully updated user: {}", existingUser.getName());
    }

    @Test(description = "Verify deleting a user")
    @Story("Delete User")
    @Severity(SeverityLevel.NORMAL)
    public void testDeleteUser() {
        int userId = 1;
        LoggerUtil.info("Testing delete user functionality for ID: {}", userId);

        Response response = userService.deleteUser(userId);

        assertEquals(response.getStatusCode(), 200, "Status code should be 200 for delete");
        LoggerUtil.logValidationPass("Delete user status", 200, response.getStatusCode());

        LoggerUtil.info("Successfully deleted user with ID: {}", userId);
    }

    @Test(description = "Verify getting user posts")
    @Story("Get User Posts")
    @Severity(SeverityLevel.NORMAL)
    public void testGetUserPosts() {
        int userId = 1;
        LoggerUtil.info("Testing get user posts for ID: {}", userId);

        Response response = userService.getUserPosts(userId);

        assertEquals(response.getStatusCode(), 200, "Status code should be 200");

        // Validate posts structure
        response.then()
                .body("size()", greaterThan(0))
                .body("[0].userId", equalTo(userId))
                .body("[0].title", notNullValue())
                .body("[0].body", notNullValue());

        int postsCount = response.jsonPath().getList("$").size();
        LoggerUtil.info("User {} has {} posts", userId, postsCount);
    }

    @Test(description = "Test user service error handling")
    @Story("Error Handling")
    @Severity(SeverityLevel.NORMAL)
    public void testUserNotFound() {
        int nonExistentUserId = 999999;
        LoggerUtil.info("Testing user not found scenario for ID: {}", nonExistentUserId);

        Response response = userService.getUserById(nonExistentUserId);

        // JSONPlaceholder returns 200 with empty object for non-existent resources
        // In real APIs, this might be 404
        assertEquals(response.getStatusCode(), 200, "Status code should be 200");

        // The response body should be empty object {}
        String responseBody = response.asString();
        assertEquals(responseBody.trim(), "{}", "Response should be empty object for non-existent user");

        LoggerUtil.info("Correctly handled non-existent user scenario");
    }
}
