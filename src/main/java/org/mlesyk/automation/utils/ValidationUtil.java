package org.mlesyk.automation.utils;

import io.restassured.response.Response;
import org.mlesyk.automation.models.User;
import org.mlesyk.automation.models.Post;
import org.mlesyk.automation.models.Comment;

import static org.testng.Assert.*;

public class ValidationUtil {

    // Response validation methods
    public static void validateSuccessResponse(Response response, int expectedStatusCode) {
        assertEquals(response.getStatusCode(), expectedStatusCode,
                "Status code should be " + expectedStatusCode);
        LoggerUtil.logValidationPass("Status code validation", expectedStatusCode, response.getStatusCode());

        assertTrue(response.getTime() < 5000, "Response time should be less than 5 seconds");
        LoggerUtil.logValidationPass("Response time validation", "< 5000ms", response.getTime() + "ms");
    }

    public static void validateErrorResponse(Response response, int expectedStatusCode, String expectedMessage) {
        assertEquals(response.getStatusCode(), expectedStatusCode,
                "Status code should be " + expectedStatusCode);

        if (expectedMessage != null) {
            assertTrue(response.asString().contains(expectedMessage),
                    "Response should contain expected error message");
            LoggerUtil.logValidationPass("Error message validation", "contains: " + expectedMessage, "found");
        }
    }

    // User validation methods
    public static void validateUser(User user) {
        assertNotNull(user, "User should not be null");
        assertNotNull(user.getId(), "User ID should not be null");
        assertNotNull(user.getName(), "User name should not be null");
        assertNotNull(user.getUsername(), "User username should not be null");
        assertNotNull(user.getEmail(), "User email should not be null");

        // Validate email format
        assertTrue(user.getEmail().contains("@"), "Email should contain @ symbol");
        assertTrue(user.getEmail().contains("."), "Email should contain domain");

        LoggerUtil.logValidationPass("User object validation", "all required fields present", "validated");
    }

    public static void validateUserStructure(User user) {
        validateUser(user);

        // Validate nested objects
        if (user.getAddress() != null) {
            assertNotNull(user.getAddress().getCity(), "Address city should not be null");
            assertNotNull(user.getAddress().getZipcode(), "Address zipcode should not be null");
            LoggerUtil.logValidationPass("User address validation", "not null", "validated");
        }

        if (user.getCompany() != null) {
            assertNotNull(user.getCompany().getName(), "Company name should not be null");
            LoggerUtil.logValidationPass("User company validation", "not null", "validated");
        }
    }

    // Post validation methods
    public static void validatePost(Post post) {
        assertNotNull(post, "Post should not be null");
        assertNotNull(post.getId(), "Post ID should not be null");
        assertNotNull(post.getTitle(), "Post title should not be null");
        assertNotNull(post.getBody(), "Post body should not be null");
        assertNotNull(post.getUserId(), "Post userId should not be null");

        assertFalse(post.getTitle().trim().isEmpty(), "Post title should not be empty");
        assertFalse(post.getBody().trim().isEmpty(), "Post body should not be empty");

        LoggerUtil.logValidationPass("Post object validation", "all required fields present", "validated");
    }

    // Comment validation methods
    public static void validateComment(Comment comment) {
        assertNotNull(comment, "Comment should not be null");
        assertNotNull(comment.getId(), "Comment ID should not be null");
        assertNotNull(comment.getName(), "Comment name should not be null");
        assertNotNull(comment.getEmail(), "Comment email should not be null");
        assertNotNull(comment.getBody(), "Comment body should not be null");
        assertNotNull(comment.getPostId(), "Comment postId should not be null");

        // Validate email format
        assertTrue(comment.getEmail().contains("@"), "Comment email should contain @ symbol");

        LoggerUtil.logValidationPass("Comment object validation", "all required fields present", "validated");
    }

    // Performance validation methods
    public static void validatePerformance(Response response, long maxResponseTime) {
        long actualResponseTime = response.getTime();
        assertTrue(actualResponseTime <= maxResponseTime,
                String.format("Response time %dms should not exceed %dms", actualResponseTime, maxResponseTime));

        LoggerUtil.logPerformanceMetric("Response Time", actualResponseTime, "ms");

        if (actualResponseTime > maxResponseTime * 0.8) {
            LoggerUtil.warn("Response time {}ms is approaching the limit of {}ms", actualResponseTime, maxResponseTime);
        }
    }

    // Collection validation methods
    public static void validateListNotEmpty(Object list, String listName) {
        assertNotNull(list, listName + " should not be null");

        if (list instanceof java.util.List) {
            java.util.List<?> listObj = (java.util.List<?>) list;
            assertFalse(listObj.isEmpty(), listName + " should not be empty");
            LoggerUtil.logValidationPass(listName + " size validation", "> 0", listObj.size());
        }
    }

    // Data integrity validation methods
    public static void validateUserPostRelationship(User user, java.util.List<Post> posts) {
        assertNotNull(user, "User should not be null");
        assertNotNull(posts, "Posts list should not be null");

        for (Post post : posts) {
            assertEquals(post.getUserId(), user.getId(),
                    "All posts should belong to the user");
        }

        LoggerUtil.logValidationPass("User-Post relationship", "all posts belong to user", "validated");
    }

    public static void validatePostCommentRelationship(Post post, java.util.List<Comment> comments) {
        assertNotNull(post, "Post should not be null");
        assertNotNull(comments, "Comments list should not be null");

        for (Comment comment : comments) {
            assertEquals(comment.getPostId(), post.getId(),
                    "All comments should belong to the post");
        }

        LoggerUtil.logValidationPass("Post-Comment relationship", "all comments belong to post", "validated");
    }
}
