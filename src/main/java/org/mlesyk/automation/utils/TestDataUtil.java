package org.mlesyk.automation.utils;

import net.datafaker.Faker;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class TestDataUtil {

    private static final Faker faker = new Faker();

    // User data generation
    public static Map<String, Object> generateUserData() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", faker.name().fullName());
        user.put("username", faker.name().username());
        user.put("email", faker.internet().emailAddress());
        user.put("phone", faker.phoneNumber().phoneNumber());
        user.put("website", faker.internet().url());

        // Address
        Map<String, Object> address = new HashMap<>();
        address.put("street", faker.address().streetAddress());
        address.put("suite", faker.address().secondaryAddress());
        address.put("city", faker.address().city());
        address.put("zipcode", faker.address().zipCode());

        // Geo coordinates
        Map<String, String> geo = new HashMap<>();
        geo.put("lat", faker.address().latitude());
        geo.put("lng", faker.address().longitude());
        address.put("geo", geo);

        user.put("address", address);

        // Company
        Map<String, Object> company = new HashMap<>();
        company.put("name", faker.company().name());
        company.put("catchPhrase", faker.company().catchPhrase());
        company.put("bs", faker.company().bs());
        user.put("company", company);

        return user;
    }

    // Post data generation
    public static Map<String, Object> generatePostData(int userId) {
        Map<String, Object> post = new HashMap<>();
        post.put("title", faker.lorem().sentence());
        post.put("body", faker.lorem().paragraph());
        post.put("userId", userId);
        return post;
    }

    // Comment data generation
    public static Map<String, Object> generateCommentData(int postId) {
        Map<String, Object> comment = new HashMap<>();
        comment.put("name", faker.lorem().sentence());
        comment.put("email", faker.internet().emailAddress());
        comment.put("body", faker.lorem().paragraph());
        comment.put("postId", postId);
        return comment;
    }

    // Album data generation
    public static Map<String, Object> generateAlbumData(int userId) {
        Map<String, Object> album = new HashMap<>();
        album.put("title", faker.book().title());
        album.put("userId", userId);
        return album;
    }

    // Photo data generation
    public static Map<String, Object> generatePhotoData(int albumId) {
        Map<String, Object> photo = new HashMap<>();
        photo.put("title", faker.lorem().sentence());
        photo.put("url", faker.internet().image());
        photo.put("thumbnailUrl", faker.internet().image(150, 150, "thumb"));
        photo.put("albumId", albumId);
        return photo;
    }

    // Bulk data generation
    public static List<Map<String, Object>> generateMultipleUsers(int count) {
        List<Map<String, Object>> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(generateUserData());
        }
        return users;
    }

    public static List<Map<String, Object>> generateMultiplePosts(int userId, int count) {
        List<Map<String, Object>> posts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            posts.add(generatePostData(userId));
        }
        return posts;
    }

    // Authentication test data
    public static Map<String, String> generateLoginCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", faker.name().username());
        credentials.put("password", faker.internet().password(8, 16, true, true, true));
        credentials.put("email", faker.internet().emailAddress());
        return credentials;
    }

    // Invalid data generation for negative testing
    public static Map<String, Object> generateInvalidUserData() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", ""); // empty name
        user.put("username", faker.lorem().characters(100)); // too long username
        user.put("email", "invalid-email"); // invalid email format
        user.put("phone", "abc123"); // invalid phone
        user.put("website", "not-a-url"); // invalid URL
        return user;
    }

    // Random data helpers
    public static String getRandomString(int length) {
        return faker.lorem().characters(length);
    }

    public static int getRandomNumber(int min, int max) {
        return faker.number().numberBetween(min, max);
    }

    public static String getRandomEmail() {
        return faker.internet().emailAddress();
    }

    public static String getRandomPhoneNumber() {
        return faker.phoneNumber().phoneNumber();
    }
}