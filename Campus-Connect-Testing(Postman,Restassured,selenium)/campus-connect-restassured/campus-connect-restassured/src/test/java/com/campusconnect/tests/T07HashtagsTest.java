package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 07 - Hashtags
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T07HashtagsTest extends BaseTest {

    @Test(priority = 1, description = "Trending hashtags")
    public void Trending_hashtags() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/hashtags/trending?limit=8"));
        assertStatus(r, 200);
    }

    @Test(priority = 2, description = "Search hashtags")
    public void Search_hashtags() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/hashtags/search?q=campus&limit=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "Posts by tag")
    public void Posts_by_tag() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/hashtags/campusconnect/posts"));
        assertStatus(r, 200);
    }
}
