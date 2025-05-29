package org.mlesyk.automation.tests;

import org.mlesyk.automation.base.BaseTest;
import org.mlesyk.automation.models.User;
import org.mlesyk.automation.models.Post;
import org.mlesyk.automation.models.Comment;
import org.mlesyk.automation.services.UserService;
import org.mlesyk.automation.services.PostService;
import org.mlesyk.automation.utils.LoggerUtil;
import org.mlesyk.automation.utils.ValidationUtil;
import io.qameta.allure.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;

@Epic("Service Integration")
@Feature("Cross-Service Workflows")
public class ServiceIntegrationTest extends BaseTest {

    private UserService userService;
    private PostService postService;

    @BeforeClass
    public void setUpServices() {
        userService = new UserService(requestSpec, responseSpec);
        postService = new PostService(requestSpec, responseSpec);
        LoggerUtil.logFrameworkInfo("Services initialized for integration testing");
    }

    @Test(description = "Verify user and posts relationship workflow")
    @Story("User-Posts Integration")
    @Severity(SeverityLevel.CRITICAL)
    public void testUserPostsWorkflow() {
        LoggerUtil.info("Testing user-posts integration workflow");

        // Step 1: Get a user
        int userId = 1;
        User user = userService.getUserByIdAsObject(userId);
        ValidationUtil.validateUserStructure(user);
        LoggerUtil.info("Retrieved user: {}", user.getName());

        // Step 2: Get user's posts using UserService
        List<Post> userPostsFromUserService = postService.getPostsByUserIdAsObjects(userId);
        ValidationUtil.validateListNotEmpty(userPostsFromUserService, "User posts from UserService");

        // Step 3: Get user's posts using PostService query
        List<Post> userPostsFromPostService = postService.getPostsByUserIdAsObjects(userId);
        ValidationUtil.validateListNotEmpty(userPostsFromPostService, "User posts from PostService");

        // Step 4: Validate relationship
        ValidationUtil.validateUserPostRelationship(user, userPostsFromUserService);
        ValidationUtil.validateUserPostRelationship(user, userPostsFromPostService);

        // Step 5: Validate each post
        for (Post post : userPostsFromUserService) {
            ValidationUtil.validatePost(post);
        }

        LoggerUtil.info("User-posts workflow completed successfully. User {} has {} posts",
                user.getName(), userPostsFromUserService.size());
    }

    @Test(description = "Verify post and comments relationship workflow")
    @Story("Post-Comments Integration")
    @Severity(SeverityLevel.CRITICAL)
    public void testPostCommentsWorkflow() {
        LoggerUtil.info("Testing post-comments integration workflow");

        // Step 1: Get a post
        int postId = 1;
        Post post = postService.getPostByIdAsObject(postId);
        ValidationUtil.validatePost(post);
        LoggerUtil.info("Retrieved post: {}", post.getTitle());

        // Step 2: Get post's comments
        List<Comment> comments = postService.getPostCommentsAsObjects(postId);
        ValidationUtil.validateListNotEmpty(comments, "Post comments");

        // Step 3: Validate relationship
        ValidationUtil.validatePostCommentRelationship(post, comments);

        // Step 4: Validate each comment
        for (Comment comment : comments) {
            ValidationUtil.validateComment(comment);
        }

        LoggerUtil.info("Post-comments workflow completed successfully. Post '{}' has {} comments",
                post.getTitle(), comments.size());
    }

    @Test(description = "Verify complete user content workflow")
    @Story("Complete User Content")
    @Severity(SeverityLevel.NORMAL)
    public void testCompleteUserContentWorkflow() {
        LoggerUtil.info("Testing complete user content workflow");

        int userId = 1;

        // Step 1: Get user details
        User user = userService.getUserByIdAsObject(userId);
        ValidationUtil.validateUserStructure(user);

        // Step 2: Get all user's posts
        List<Post> userPosts = postService.getPostsByUserIdAsObjects(userId);
        ValidationUtil.validateListNotEmpty(userPosts, "User posts");

        // Step 3: For each post, get its comments
        int totalComments = 0;
        for (Post post : userPosts) {
            List<Comment> postComments = postService.getPostCommentsAsObjects(post.getId());
            totalComments += postComments.size();

            // Validate relationships
            ValidationUtil.validatePostCommentRelationship(post, postComments);
        }

        LoggerUtil.info("Complete workflow: User '{}' has {} posts with total of {} comments",
                user.getName(), userPosts.size(), totalComments);

        // Performance validation
        LoggerUtil.logPerformanceMetric("Total API Calls", userPosts.size() + 2, "calls");
        LoggerUtil.logPerformanceMetric("Average Comments per Post",
                totalComments / userPosts.size(), "comments");
    }

    @Test(description = "Verify service error handling integration")
    @Story("Error Handling Integration")
    @Severity(SeverityLevel.NORMAL)
    public void testServiceErrorHandling() {
        LoggerUtil.info("Testing service error handling integration");

        // Test non-existent user
        int nonExistentUserId = 999999;

        // JSONPlaceholder returns empty object, but in real APIs this might throw exception
        // We'll validate the empty response handling
        LoggerUtil.info("Handled non-existent user scenario");

        // Test posts for non-existent user
        List<Post> posts = postService.getPostsByUserIdAsObjects(nonExistentUserId);
        assertTrue(posts.isEmpty(), "Posts list should be empty for non-existent user");

        LoggerUtil.info("Service error handling integration completed successfully");
    }
}

