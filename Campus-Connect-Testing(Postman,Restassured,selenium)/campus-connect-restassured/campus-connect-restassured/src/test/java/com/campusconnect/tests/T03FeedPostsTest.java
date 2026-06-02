package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 03 - Feed & Posts
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T03FeedPostsTest extends BaseTest {

    @Test(priority = 1, description = "U1 create post")
    public void U1_create_post() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"content\": \"Hello from automated test #campusconnect\",\n  \"postType\": \"GENERAL\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/posts"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "postId");
    }

    @Test(priority = 2, description = "U1 create poll-type post")
    public void U1_create_poll_type_post() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"content\": \"Poll post for tests\",\n  \"postType\": \"POLL\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/posts"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "pollPostId");
    }

    @Test(priority = 3, description = "Get feed (ALL)")
    public void Get_feed_ALL() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/feed?page=0&size=10&mode=ALL"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "Get feed (FOR_YOU)")
    public void Get_feed_FOR_YOU() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/feed?page=0&size=10&mode=FOR_YOU"));
        assertStatus(r, 200);
    }

    @Test(priority = 5, description = "Get public feed")
    public void Get_public_feed() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/feed/public?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 6, description = "Get user posts")
    public void Get_user_posts() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/users/{{user1Id}}/posts?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 7, description = "Get single post")
    public void Get_single_post() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/posts/{{postId}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 8, description = "U2 like post")
    public void U2_like_post() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().post(url("{{baseUrl}}/api/posts/{{postId}}/like"));
        assertStatus(r, 200);
    }

    @Test(priority = 9, description = "U2 unlike post (toggle)")
    public void U2_unlike_post_toggle() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().post(url("{{baseUrl}}/api/posts/{{postId}}/like"));
        assertStatus(r, 200);
    }

    @Test(priority = 10, description = "NEG Get nonexistent post")
    public void NEG_Get_nonexistent_post() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/posts/99999999"));
        assertStatus(r, 404, 400);
    }

    @Test(priority = 11, description = "NEG Create post without token")
    public void NEG_Create_post_without_token() {
        RequestSpecification spec = noAuth();
        spec = json(spec, "{\n  \"content\": \"x\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/posts"));
        assertStatus(r, 401, 403);
    }
}
