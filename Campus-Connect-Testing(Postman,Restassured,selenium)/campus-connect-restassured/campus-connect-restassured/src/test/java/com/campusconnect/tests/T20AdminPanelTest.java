package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 20 - Admin Panel
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T20AdminPanelTest extends BaseTest {

    @Test(priority = 1, description = "Admin stats")
    public void Admin_stats() {
        RequestSpecification spec = auth("adminToken");
        Response r = spec.when().get(url("{{baseUrl}}/api/admin/stats"));
        assertStatus(r, 200);
        assertHasProperty(r, "totalUsers");
    }

    @Test(priority = 2, description = "Admin list users")
    public void Admin_list_users() {
        RequestSpecification spec = auth("adminToken");
        Response r = spec.when().get(url("{{baseUrl}}/api/admin/users?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "Admin search users by status")
    public void Admin_search_users_by_status() {
        RequestSpecification spec = auth("adminToken");
        Response r = spec.when().get(url("{{baseUrl}}/api/admin/users?status=ACTIVE&page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "Admin search users by role")
    public void Admin_search_users_by_role() {
        RequestSpecification spec = auth("adminToken");
        Response r = spec.when().get(url("{{baseUrl}}/api/admin/users?role=MANAGER&page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 5, description = "Admin get one user")
    public void Admin_get_one_user() {
        RequestSpecification spec = auth("adminToken");
        Response r = spec.when().get(url("{{baseUrl}}/api/admin/users/{{user2Id}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 6, description = "Admin suspend U2")
    public void Admin_suspend_U2() {
        RequestSpecification spec = auth("adminToken");
        spec = json(spec, "{\n  \"active\": false,\n  \"reason\": \"policy violation (test)\"\n}");
        Response r = spec.when().put(url("{{baseUrl}}/api/admin/users/{{user2Id}}/status"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 7, description = "Verify suspended U2 token rejected")
    public void Verify_suspended_U2_token_rejected() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().get(url("{{baseUrl}}/api/users/me"));
        assertStatus(r, 401, 403);
    }

    @Test(priority = 8, description = "Admin reactivate U2")
    public void Admin_reactivate_U2() {
        RequestSpecification spec = auth("adminToken");
        spec = json(spec, "{\n  \"active\": true,\n  \"reason\": \"reinstated (test)\"\n}");
        Response r = spec.when().put(url("{{baseUrl}}/api/admin/users/{{user2Id}}/status"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 9, description = "Verify reactivated U2 works")
    public void Verify_reactivated_U2_works() {
        RequestSpecification spec = noAuth();
        spec = json(spec, "{\n  \"email\": \"{{user2Email}}\",\n  \"password\": \"{{password}}\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/auth/login"));
        assertStatus(r, 200);
        saveIfPresent(r, "token", "token2");
    }

    @Test(priority = 10, description = "Admin change U2 role")
    public void Admin_change_U2_role() {
        RequestSpecification spec = auth("adminToken");
        spec = json(spec, "{\n  \"role\": \"SENIOR_ASSOCIATE\"\n}");
        Response r = spec.when().put(url("{{baseUrl}}/api/admin/users/{{user2Id}}/role"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 11, description = "Admin list reports")
    public void Admin_list_reports() {
        RequestSpecification spec = auth("adminToken");
        Response r = spec.when().get(url("{{baseUrl}}/api/admin/reports?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 12, description = "Admin resolve report")
    public void Admin_resolve_report() {
        RequestSpecification spec = auth("adminToken");
        Response r = spec.when().put(url("{{baseUrl}}/api/admin/reports/{{reportId}}?status=RESOLVED"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 13, description = "Admin list posts (content)")
    public void Admin_list_posts_content() {
        RequestSpecification spec = auth("adminToken");
        Response r = spec.when().get(url("{{baseUrl}}/api/admin/content/posts?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 14, description = "Admin remove a post")
    public void Admin_remove_a_post() {
        RequestSpecification spec = auth("adminToken");
        Response r = spec.when().delete(url("{{baseUrl}}/api/admin/content/POST/{{postId}}"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 15, description = "Admin audit log")
    public void Admin_audit_log() {
        RequestSpecification spec = auth("adminToken");
        Response r = spec.when().get(url("{{baseUrl}}/api/admin/logs?page=0&size=30"));
        assertStatus(r, 200);
    }

    @Test(priority = 16, description = "NEG Non-admin hits admin stats")
    public void NEG_Non_admin_hits_admin_stats() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/admin/stats"));
        assertStatus(r, 403);
    }

    @Test(priority = 17, description = "NEG No token on admin endpoint")
    public void NEG_No_token_on_admin_endpoint() {
        RequestSpecification spec = noAuth();
        Response r = spec.when().get(url("{{baseUrl}}/api/admin/users"));
        assertStatus(r, 401, 403);
    }

    @Test(priority = 18, description = "NEG Admin suspends self")
    public void NEG_Admin_suspends_self() {
        RequestSpecification spec = auth("adminToken");
        spec = json(spec, "{\n  \"active\": false,\n  \"reason\": \"self\"\n}");
        Response r = spec.when().put(url("{{baseUrl}}/api/admin/users/{{adminId}}/status"));
        assertStatus(r, 400);
    }

    @Test(priority = 19, description = "NEG Admin resolve nonexistent report")
    public void NEG_Admin_resolve_nonexistent_report() {
        RequestSpecification spec = auth("adminToken");
        Response r = spec.when().put(url("{{baseUrl}}/api/admin/reports/99999999?status=RESOLVED"));
        assertStatus(r, 404, 400);
    }

    @Test(priority = 20, description = "NEG Admin invalid role change")
    public void NEG_Admin_invalid_role_change() {
        RequestSpecification spec = auth("adminToken");
        spec = json(spec, "{\n  \"role\": \"WIZARD\"\n}");
        Response r = spec.when().put(url("{{baseUrl}}/api/admin/users/{{user2Id}}/role"));
        assertStatus(r, 400);
    }

    @Test(priority = 21, description = "NEG Admin remove unsupported content type")
    public void NEG_Admin_remove_unsupported_content_type() {
        RequestSpecification spec = auth("adminToken");
        Response r = spec.when().delete(url("{{baseUrl}}/api/admin/content/BOOK/1"));
        assertStatus(r, 400, 404);
    }
}
