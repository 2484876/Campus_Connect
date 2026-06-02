package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 16 - Career
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T16CareerTest extends BaseTest {

    @Test(priority = 1, description = "My profile completion")
    public void My_profile_completion() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/career/completion/me"));
        assertStatus(r, 200);
    }

    @Test(priority = 2, description = "User profile completion")
    public void User_profile_completion() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/career/completion/user/{{user2Id}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "Search by skill")
    public void Search_by_skill() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/career/skills/search?skill=Java&page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "Skill autocomplete")
    public void Skill_autocomplete() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/career/skills/autocomplete?q=ja&limit=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 5, description = "Trending skills")
    public void Trending_skills() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/career/skills/trending?limit=10"));
        assertStatus(r, 200);
    }
}
