package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 02 - Connections
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T02ConnectionsTest extends BaseTest {

    @Test(priority = 1, description = "U1 send connection request to U2")
    public void U1_send_connection_request_to_U2() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"receiverId\": \"{{user2Id}}\",\n  \"message\": \"Let's connect\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/connections/request"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "connectionId", "connectionId");
        saveIfPresent(r, "id", "connectionId");
    }

    @Test(priority = 2, description = "U2 view pending requests")
    public void U2_view_pending_requests() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().get(url("{{baseUrl}}/api/connections/pending?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "U1 view sent requests")
    public void U1_view_sent_requests() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/connections/sent?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "U2 accept request")
    public void U2_accept_request() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().put(url("{{baseUrl}}/api/connections/{{connectionId}}/accept"));
        assertStatus(r, 200);
    }

    @Test(priority = 5, description = "U1 list connections")
    public void U1_list_connections() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/connections?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 6, description = "U1 suggestions")
    public void U1_suggestions() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/connections/suggestions?limit=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 7, description = "Mutual connections")
    public void Mutual_connections() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/connections/mutuals/{{user2Id}}?limit=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 8, description = "Mutual count")
    public void Mutual_count() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/connections/mutuals/{{user2Id}}/count"));
        assertStatus(r, 200);
    }

    @Test(priority = 9, description = "NEG Connect to self")
    public void NEG_Connect_to_self() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"receiverId\": \"{{user1Id}}\",\n  \"message\": \"self\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/connections/request"));
        assertStatus(r, 400);
    }

    @Test(priority = 10, description = "NEG Duplicate connection")
    public void NEG_Duplicate_connection() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"receiverId\": \"{{user2Id}}\",\n  \"message\": \"again\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/connections/request"));
        assertStatus(r, 400);
    }
}
