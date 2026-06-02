package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 06 - Bookmarks
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T06BookmarksTest extends BaseTest {

    @Test(priority = 1, description = "Toggle bookmark (add)")
    public void Toggle_bookmark_add() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().post(url("{{baseUrl}}/api/bookmarks/{{postId}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 2, description = "List my bookmarks")
    public void List_my_bookmarks() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/bookmarks?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "Bookmark count")
    public void Bookmark_count() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/bookmarks/count"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "Toggle bookmark (remove)")
    public void Toggle_bookmark_remove() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().post(url("{{baseUrl}}/api/bookmarks/{{postId}}"));
        assertStatus(r, 200);
    }
}
