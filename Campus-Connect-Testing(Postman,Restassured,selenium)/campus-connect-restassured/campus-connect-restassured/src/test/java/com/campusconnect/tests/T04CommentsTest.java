package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 04 - Comments
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T04CommentsTest extends BaseTest {

    @Test(priority = 1, description = "U2 add comment")
    public void U2_add_comment() {
        RequestSpecification spec = auth("token2");
        spec = json(spec, "{\n  \"content\": \"Nice post!\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/posts/{{postId}}/comments"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "commentId");
    }

    @Test(priority = 2, description = "Get comments")
    public void Get_comments() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/posts/{{postId}}/comments?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "NEG Empty comment")
    public void NEG_Empty_comment() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"content\": \"\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/posts/{{postId}}/comments"));
        assertStatus(r, 400);
    }
}
