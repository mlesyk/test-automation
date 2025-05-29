package org.mlesyk.automation.services;

import org.mlesyk.automation.models.User;
import org.mlesyk.automation.utils.LoggerUtil;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class UserService extends BaseService {

    private static final String USERS_ENDPOINT = "/users";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserService(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        super(requestSpec, responseSpec);
    }

    // Get all users
    public Response getAllUsers() {
        LoggerUtil.info("Getting all users");
        return performGet(USERS_ENDPOINT);
    }

    public List<User> getAllUsersAsObjects() {
        Response response = getAllUsers();
        try {
            return objectMapper.readValue(response.asString(), new TypeReference<List<User>>() {});
        } catch (Exception e) {
            LoggerUtil.error("Failed to parse users response", e);
            throw new RuntimeException("Failed to parse users response", e);
        }
    }

    // Get user by ID
    public Response getUserById(int userId) {
        LoggerUtil.info("Getting user with ID: {}", userId);
        return performGet(USERS_ENDPOINT + "/" + userId);
    }

    public User getUserByIdAsObject(int userId) {
        Response response = getUserById(userId);
        try {
            return objectMapper.readValue(response.asString(), User.class);
        } catch (Exception e) {
            LoggerUtil.error("Failed to parse user response for ID: {}", userId, e);
            throw new RuntimeException("Failed to parse user response", e);
        }
    }

    // Create new user
    public Response createUser(User user) {
        LoggerUtil.info("Creating new user: {}", user.getName());
        return performPost(USERS_ENDPOINT, user);
    }

    public User createUserAndReturn(User user) {
        Response response = createUser(user);
        try {
            return objectMapper.readValue(response.asString(), User.class);
        } catch (Exception e) {
            LoggerUtil.error("Failed to parse created user response", e);
            throw new RuntimeException("Failed to parse created user response", e);
        }
    }

    // Update user
    public Response updateUser(int userId, User user) {
        LoggerUtil.info("Updating user with ID: {}", userId);
        return performPut(USERS_ENDPOINT + "/" + userId, user);
    }

    public User updateUserAndReturn(int userId, User user) {
        Response response = updateUser(userId, user);
        try {
            return objectMapper.readValue(response.asString(), User.class);
        } catch (Exception e) {
            LoggerUtil.error("Failed to parse updated user response", e);
            throw new RuntimeException("Failed to parse updated user response", e);
        }
    }

    // Partial update user
    public Response patchUser(int userId, Object partialUser) {
        LoggerUtil.info("Partially updating user with ID: {}", userId);
        return performPatch(USERS_ENDPOINT + "/" + userId, partialUser);
    }

    // Delete user
    public Response deleteUser(int userId) {
        LoggerUtil.info("Deleting user with ID: {}", userId);
        return performDelete(USERS_ENDPOINT + "/" + userId);
    }

    // Get user posts
    public Response getUserPosts(int userId) {
        LoggerUtil.info("Getting posts for user ID: {}", userId);
        return performGet(USERS_ENDPOINT + "/" + userId + "/posts");
    }

    // Get user albums
    public Response getUserAlbums(int userId) {
        LoggerUtil.info("Getting albums for user ID: {}", userId);
        return performGet(USERS_ENDPOINT + "/" + userId + "/albums");
    }

    // Get user todos
    public Response getUserTodos(int userId) {
        LoggerUtil.info("Getting todos for user ID: {}", userId);
        return performGet(USERS_ENDPOINT + "/" + userId + "/todos");
    }
}