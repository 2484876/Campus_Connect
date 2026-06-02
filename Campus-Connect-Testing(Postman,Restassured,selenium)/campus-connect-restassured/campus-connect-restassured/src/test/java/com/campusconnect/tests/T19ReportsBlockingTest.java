package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 19 - Reports & Blocking
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T19ReportsBlockingTest extends BaseTest {

    @Test(priority = 1, description = "U2 report U1 post")
    public void U2_report_U1_post() {
        RequestSpecification spec = auth("token2");
        spec = json(spec, "{\n  \"targetType\": \"POST\",\n  \"targetId\": \"{{postId}}\",\n  \"reason\": \"SPAM\",\n  \"details\": \"test report\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/reports"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "reportId");
    }

    @Test(priority = 2, description = "U1 block U2")
    public void U1_block_U2() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().post(url("{{baseUrl}}/api/users/{{user2Id}}/block"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "U1 block-status of U2")
    public void U1_block_status_of_U2() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/users/{{user2Id}}/block-status"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "U1 unblock U2")
    public void U1_unblock_U2() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().delete(url("{{baseUrl}}/api/users/{{user2Id}}/block"));
        assertStatus(r, 200);
    }

    @Test(priority = 5, description = "NEG Block self")
    public void NEG_Block_self() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().post(url("{{baseUrl}}/api/users/{{user1Id}}/block"));
        assertStatus(r, 400);
    }

    @Test(priority = 6, description = "NEG Duplicate report")
    public void NEG_Duplicate_report() {
        RequestSpecification spec = auth("token2");
        spec = json(spec, "{\n  \"targetType\": \"POST\",\n  \"targetId\": \"{{postId}}\",\n  \"reason\": \"SPAM\",\n  \"details\": \"again\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/reports"));
        assertStatus(r, 400);
    }
}
