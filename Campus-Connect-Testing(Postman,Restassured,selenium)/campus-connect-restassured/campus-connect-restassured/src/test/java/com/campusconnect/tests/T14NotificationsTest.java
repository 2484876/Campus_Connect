package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 14 - Notifications
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T14NotificationsTest extends BaseTest {

    @Test(priority = 1, description = "U2 get notifications")
    public void U2_get_notifications() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().get(url("{{baseUrl}}/api/notifications?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 2, description = "U2 unread count")
    public void U2_unread_count() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().get(url("{{baseUrl}}/api/notifications/unread-count"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "U2 mark all read")
    public void U2_mark_all_read() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().put(url("{{baseUrl}}/api/notifications/read"));
        assertStatus(r, 200, 204);
    }
}
