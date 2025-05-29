package org.mlesyk.automation.services;

import org.mlesyk.automation.models.Post;
import org.mlesyk.automation.models.Comment;
import org.mlesyk.automation.utils.LoggerUtil;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class PostService extends BaseService {

    private static final String POSTS_ENDPOINT = "/posts";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PostService(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        super(requestSpec, responseSpec);
    }

    // Get all posts
    public Response getAllPosts() {
        LoggerUtil.info("Getting all posts");
        return performGet(POSTS_ENDPOINT);
    }

    public List<Post> getAllPostsAsObjects() {
        Response response = getAllPosts();
        try {
            return objectMapper.readValue(response.asString(), new TypeReference<List<Post>>() {});
        } catch (Exception e) {
            LoggerUtil.error("Failed to parse posts response", e);
            throw new RuntimeException("Failed to parse posts response", e);
        }
    }

    // Get post by ID
    public Response getPostById(int postId) {
        LoggerUtil.info("Getting post with ID: {}", postId);
        return performGet(POSTS_ENDPOINT + "/" + postId);
    }

    public Post getPostByIdAsObject(int postId) {
        Response response = getPostById(postId);
        try {
            return objectMapper.readValue(response.asString(), Post.class);
        } catch (Exception e) {
            LoggerUtil.error("Failed to parse post response for ID: {}", postId, e);
            throw new RuntimeException("Failed to parse post response", e);
        }
    }

    // Create new post
    public Response createPost(Post post) {
        LoggerUtil.info("Creating new post: {}", post.getTitle());
        return performPost(POSTS_ENDPOINT, post);
    }

    public Post createPostAndReturn(Post post) {
        Response response = createPost(post);
        try {
            return objectMapper.readValue(response.asString(), Post.class);
        } catch (Exception e) {
            LoggerUtil.error("Failed to parse created post response", e);
            throw new RuntimeException("Failed to parse created post response", e);
        }
    }

    // Update post
    public Response updatePost(int postId, Post post) {
        LoggerUtil.info("Updating post with ID: {}", postId);
        return performPut(POSTS_ENDPOINT + "/" + postId, post);
    }

    // Delete post
    public Response deletePost(int postId) {
        LoggerUtil.info("Deleting post with ID: {}", postId);
        return performDelete(POSTS_ENDPOINT + "/" + postId);
    }

    // Get post comments
    public Response getPostComments(int postId) {
        LoggerUtil.info("Getting comments for post ID: {}", postId);
        return performGet(POSTS_ENDPOINT + "/" + postId + "/comments");
    }

    public List<Comment> getPostCommentsAsObjects(int postId) {
        Response response = getPostComments(postId);
        try {
            return objectMapper.readValue(response.asString(), new TypeReference<List<Comment>>() {});
        } catch (Exception e) {
            LoggerUtil.error("Failed to parse comments response for post ID: {}", postId, e);
            throw new RuntimeException("Failed to parse comments response", e);
        }
    }

    // Get posts by user ID
    public Response getPostsByUserId(int userId) {
        LoggerUtil.info("Getting posts for user ID: {}", userId);
        return performGet(POSTS_ENDPOINT + "?userId=" + userId);
    }

    public List<Post> getPostsByUserIdAsObjects(int userId) {
        Response response = getPostsByUserId(userId);
        try {
            return objectMapper.readValue(response.asString(), new TypeReference<List<Post>>() {});
        } catch (Exception e) {
            LoggerUtil.error("Failed to parse posts response for user ID: {}", userId, e);
            throw new RuntimeException("Failed to parse posts response", e);
        }
    }
}
