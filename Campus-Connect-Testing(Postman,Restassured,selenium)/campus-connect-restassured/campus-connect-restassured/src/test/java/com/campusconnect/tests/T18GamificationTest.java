package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 18 - Gamification
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T18GamificationTest extends BaseTest {

    @Test(priority = 1, description = "My achievements")
    public void My_achievements() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/achievements/me"));
        assertStatus(r, 200);
    }

    @Test(priority = 2, description = "Achievements for user")
    public void Achievements_for_user() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/achievements/user/{{user2Id}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "Achievement stats")
    public void Achievement_stats() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/achievements/stats/{{user2Id}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "Streak check-in")
    public void Streak_check_in() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().post(url("{{baseUrl}}/api/streak/check-in"));
        assertStatus(r, 200);
    }

    @Test(priority = 5, description = "My streak")
    public void My_streak() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/streak/me"));
        assertStatus(r, 200);
    }

    @Test(priority = 6, description = "Celebrants")
    public void Celebrants() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/users/celebrants"));
        assertStatus(r, 200);
    }
}
