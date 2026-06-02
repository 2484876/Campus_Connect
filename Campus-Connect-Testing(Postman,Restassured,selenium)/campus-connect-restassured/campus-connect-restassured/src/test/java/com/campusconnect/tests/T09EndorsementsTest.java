package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 09 - Endorsements
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T09EndorsementsTest extends BaseTest {

    @Test(priority = 1, description = "U1 endorse U2 skill")
    public void U1_endorse_U2_skill() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"endorseeId\": \"{{user2Id}}\",\n  \"skill\": \"Java\",\n  \"category\": \"TECHNICAL\",\n  \"message\": \"Strong Java skills\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/endorsements"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "endorsementId");
    }

    @Test(priority = 2, description = "Endorsements received by U2")
    public void Endorsements_received_by_U2() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/endorsements/user/{{user2Id}}?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "Endorsements by endorser")
    public void Endorsements_by_endorser() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/endorsements/user/{{user2Id}}/by/{{user1Id}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "Endorsers for skill")
    public void Endorsers_for_skill() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/endorsements/user/{{user2Id}}/skill/Java"));
        assertStatus(r, 200);
    }

    @Test(priority = 5, description = "Endorsement summary")
    public void Endorsement_summary() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/endorsements/user/{{user2Id}}/summary"));
        assertStatus(r, 200);
    }

    @Test(priority = 6, description = "Remove endorsement")
    public void Remove_endorsement() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().delete(url("{{baseUrl}}/api/endorsements/{{endorsementId}}"));
        assertStatus(r, 200, 204);
    }
}
