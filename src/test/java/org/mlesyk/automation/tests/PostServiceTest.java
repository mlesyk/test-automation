package org.mlesyk.automation.tests;

import org.mlesyk.automation.base.BaseTest;
import org.mlesyk.automation.models.Post;
import org.mlesyk.automation.models.Comment;
import org.mlesyk.automation.services.PostService;
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

@Epic("Content Management")
@Feature("Post Service")
public class PostServiceTest extends BaseTest {

    private PostService postService;

    @BeforeClass
    public void setUpPostService() {
        postService = new PostService(requestSpec, responseSpec);
        LoggerUtil.logFrameworkInfo("PostService initialized for testing");
    }

    @Test(description = "Verify getting all posts")
    @Story("Get Posts")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetAllPosts() {
        LoggerUtil.info("Testing get all posts functionality");

        Response response = postService.getAllPosts();

        assertEquals(response.getStatusCode(), 200, "Status code should be 200");

        // Validate posts structure
        response.then()
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue())
                .body("[0].title", notNullValue())
                .body("[0].body", notNullValue())
                .body("[0].userId", notNullValue());

        List<Post> posts = postService.getAllPostsAsObjects();
        assertFalse(posts.isEmpty(), "Posts list should not be empty");
        LoggerUtil.info("Successfully retrieved {} posts", posts.size());
    }

    @Test(description = "Verify creating a new post")
    @Story("Create Post")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreatePost() {
        LoggerUtil.info("Testing create post functionality");

        // Generate test data
        Map<String, Object> postData = TestDataUtil.generatePostData(1);

        Post newPost = new Post();
        newPost.setTitle((String) postData.get("title"));
        newPost.setBody((String) postData.get("body"));
        newPost.setUserId((Integer) postData.get("userId"));

        Response response = postService.createPost(newPost);

        assertEquals(response.getStatusCode(), 201, "Status code should be 201 for creation");

        // Validate created post
        response.then()
                .body("title", equalTo(newPost.getTitle()))
                .body("body", equalTo(newPost.getBody()))
                .body("userId", equalTo(newPost.getUserId()))
                .body("id", notNullValue());

        Post createdPost = postService.createPostAndReturn(newPost);
        assertNotNull(createdPost.getId(), "Created post should have an ID");

        LoggerUtil.info("Successfully created post with ID: {}", createdPost.getId());
    }

    @Test(description = "Verify getting post comments")
    @Story("Get Post Comments")
    @Severity(SeverityLevel.NORMAL)
    public void testGetPostComments() {
        int postId = 1;
        LoggerUtil.info("Testing get post comments for ID: {}", postId);

        Response response = postService.getPostComments(postId);

        assertEquals(response.getStatusCode(), 200, "Status code should be 200");

        // Validate comments structure
        response.then()
                .body("size()", greaterThan(0))
                .body("[0].postId", equalTo(postId))
                .body("[0].name", notNullValue())
                .body("[0].email", notNullValue())
                .body("[0].body", notNullValue());

        List<Comment> comments = postService.getPostCommentsAsObjects(postId);
        assertFalse(comments.isEmpty(), "Comments list should not be empty");

        LoggerUtil.info("Post {} has {} comments", postId, comments.size());
    }

    @Test(description = "Verify getting posts by user ID")
    @Story("Get Posts by User")
    @Severity(SeverityLevel.NORMAL)
    public void testGetPostsByUserId() {
        int userId = 1;
        LoggerUtil.info("Testing get posts by user ID: {}", userId);

        Response response = postService.getPostsByUserId(userId);

        assertEquals(response.getStatusCode(), 200, "Status code should be 200");

        // Validate all posts belong to the user
        response.then()
                .body("size()", greaterThan(0))
                .body("findAll { it.userId == " + userId + " }.size()", equalTo(response.jsonPath().getList("$").size()));

        List<Post> userPosts = postService.getPostsByUserIdAsObjects(userId);
        assertFalse(userPosts.isEmpty(), "User posts list should not be empty");

        // Verify all posts belong to the correct user
        for (Post post : userPosts) {
            assertEquals(post.getUserId(), Integer.valueOf(userId),
                    "All posts should belong to user " + userId);
        }

        LoggerUtil.info("User {} has {} posts", userId, userPosts.size());
    }

    @Test(description = "Verify post update functionality")
    @Story("Update Post")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdatePost() {
        int postId = 1;
        LoggerUtil.info("Testing update post functionality for ID: {}", postId);

        // Get existing post
        Post existingPost = postService.getPostByIdAsObject(postId);
        LoggerUtil.info("Retrieved existing post: {}", existingPost.getTitle());

        // Modify post data
        existingPost.setTitle("Updated: " + existingPost.getTitle());
        existingPost.setBody("Updated: " + existingPost.getBody());

        Response response = postService.updatePost(postId, existingPost);

        assertEquals(response.getStatusCode(), 200, "Status code should be 200 for update");

        // Validate updated response
        response.then()
                .body("id", equalTo(postId))
                .body("title", equalTo(existingPost.getTitle()))
                .body("body", equalTo(existingPost.getBody()));

        LoggerUtil.info("Successfully updated post: {}", existingPost.getTitle());
    }

    @Test(description = "Verify post deletion")
    @Story("Delete Post")
    @Severity(SeverityLevel.NORMAL)
    public void testDeletePost() {
        int postId = 1;
        LoggerUtil.info("Testing delete post functionality for ID: {}", postId);

        Response response = postService.deletePost(postId);

        assertEquals(response.getStatusCode(), 200, "Status code should be 200 for delete");
        LoggerUtil.logValidationPass("Delete post status", 200, response.getStatusCode());

        LoggerUtil.info("Successfully deleted post with ID: {}", postId);
    }
}
