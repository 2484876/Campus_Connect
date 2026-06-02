package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 05 - Polls
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T05PollsTest extends BaseTest {

    @Test(priority = 1, description = "Create poll on post")
    public void Create_poll_on_post() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"question\": \"Best backend framework?\",\n  \"options\": [\n    \"Spring\",\n    \"Express\",\n    \"Django\"\n  ],\n  \"multiChoice\": false\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/polls/posts/{{pollPostId}}"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "pollId");
        saveIfPresent(r, "options[0].id", "pollOptionId");
    }

    @Test(priority = 2, description = "Get poll for post")
    public void Get_poll_for_post() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/polls/posts/{{pollPostId}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "Vote on poll")
    public void Vote_on_poll() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().post(url("{{baseUrl}}/api/polls/{{pollId}}/vote/{{pollOptionId}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "NEG Vote on nonexistent poll")
    public void NEG_Vote_on_nonexistent_poll() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().post(url("{{baseUrl}}/api/polls/99999999/vote/1"));
        assertStatus(r, 404, 400);
    }
}
