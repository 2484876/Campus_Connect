package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 15 - Presence
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T15PresenceTest extends BaseTest {

    @Test(priority = 1, description = "Heartbeat")
    public void Heartbeat() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().post(url("{{baseUrl}}/api/presence/heartbeat"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 2, description = "Set status")
    public void Set_status() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"status\": \"ONLINE\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/presence/status"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 3, description = "Get one presence")
    public void Get_one_presence() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/presence/{{user2Id}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "Bulk presence")
    public void Bulk_presence() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"userIds\": [\n    \"{{user1Id}}\",\n    \"{{user2Id}}\"\n  ]\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/presence/bulk"));
        assertStatus(r, 200);
    }
}
