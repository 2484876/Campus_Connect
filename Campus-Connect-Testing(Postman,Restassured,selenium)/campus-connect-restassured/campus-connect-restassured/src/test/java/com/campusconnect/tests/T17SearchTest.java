package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 17 - Search
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T17SearchTest extends BaseTest {

    @Test(priority = 1, description = "Global search")
    public void Global_search() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/search?q=Tester"));
        assertStatus(r, 200);
    }
}
