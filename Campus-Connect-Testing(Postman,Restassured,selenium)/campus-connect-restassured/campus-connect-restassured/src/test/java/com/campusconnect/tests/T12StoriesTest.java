package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 12 - Stories
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T12StoriesTest extends BaseTest {

    @Test(priority = 1, description = "U1 create story")
    public void U1_create_story() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"mediaUrl\": \"https://picsum.photos/400/700\",\n  \"caption\": \"Test story\",\n  \"backgroundColor\": \"#2d5f3f\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/stories"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "storyId");
    }

    @Test(priority = 2, description = "Stories feed")
    public void Stories_feed() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/stories/feed"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "U2 mark story viewed")
    public void U2_mark_story_viewed() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().post(url("{{baseUrl}}/api/stories/{{storyId}}/view"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 4, description = "Story viewers")
    public void Story_viewers() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/stories/{{storyId}}/viewers"));
        assertStatus(r, 200);
    }

    @Test(priority = 5, description = "NEG U2 delete U1 story")
    public void NEG_U2_delete_U1_story() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().delete(url("{{baseUrl}}/api/stories/{{storyId}}"));
        assertStatus(r, 400, 403);
    }

    @Test(priority = 6, description = "U1 delete story")
    public void U1_delete_story() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().delete(url("{{baseUrl}}/api/stories/{{storyId}}"));
        assertStatus(r, 200, 204);
    }
}
