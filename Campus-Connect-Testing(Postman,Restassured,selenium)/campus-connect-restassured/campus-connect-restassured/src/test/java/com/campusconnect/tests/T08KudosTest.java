package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 08 - Kudos
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T08KudosTest extends BaseTest {

    @Test(priority = 1, description = "U1 give kudos to U2")
    public void U1_give_kudos_to_U2() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"receiverId\": \"{{user2Id}}\",\n  \"category\": \"TEAMWORK\",\n  \"message\": \"Great collaboration!\",\n  \"isPublic\": true\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/kudos"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "kudosId");
    }

    @Test(priority = 2, description = "Kudos received by U2")
    public void Kudos_received_by_U2() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/kudos/received/{{user2Id}}?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "Kudos given by U1")
    public void Kudos_given_by_U1() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/kudos/given/{{user1Id}}?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "Recent kudos")
    public void Recent_kudos() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/kudos/recent?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 5, description = "Kudos stats")
    public void Kudos_stats() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/kudos/stats/{{user2Id}}"));
        assertStatus(r, 200);
    }
}
