package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 00 - Auth & Setup
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T00AuthSetupTest extends BaseTest {

    @Test(priority = 1, description = "Register User 1")
    public void Register_User_1() {
        RequestSpecification spec = noAuth();
        spec = json(spec, "{\n  \"name\": \"Alice Tester\",\n  \"email\": \"{{user1Email}}\",\n  \"password\": \"{{password}}\",\n  \"role\": \"ASSOCIATE\",\n  \"department\": \"Engineering\",\n  \"position\": \"Backend Dev\",\n  \"phone\": \"9000000001\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/auth/register"));
        // backend returns 201 CREATED on register
        assertStatus(r, 200, 201);
        assertHasProperty(r, "token");
        saveIfPresent(r, "token", "token1");
        saveIfPresent(r, "userId", "user1Id");
    }

    @Test(priority = 2, description = "Register User 2")
    public void Register_User_2() {
        RequestSpecification spec = noAuth();
        spec = json(spec, "{\n  \"name\": \"Bob Tester\",\n  \"email\": \"{{user2Email}}\",\n  \"password\": \"{{password}}\",\n  \"role\": \"MANAGER\",\n  \"department\": \"Engineering\",\n  \"position\": \"Team Lead\",\n  \"phone\": \"9000000002\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/auth/register"));
        // backend returns 201 CREATED on register
        assertStatus(r, 200, 201);
        saveIfPresent(r, "token", "token2");
        saveIfPresent(r, "userId", "user2Id");
    }

    @Test(priority = 3, description = "Register Admin")
    public void Register_Admin() {
        RequestSpecification spec = noAuth();
        spec = json(spec, "{\n  \"name\": \"Admin Root\",\n  \"email\": \"{{adminEmail}}\",\n  \"password\": \"{{password}}\",\n  \"role\": \"ADMIN\",\n  \"department\": \"Ops\",\n  \"position\": \"Administrator\",\n  \"phone\": \"9000000003\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/auth/register"));
        // backend returns 201 CREATED on register
        assertStatus(r, 200, 201);
        saveIfPresent(r, "token", "adminToken");
        saveIfPresent(r, "userId", "adminId");
    }

    @Test(priority = 4, description = "Login User 1")
    public void Login_User_1() {
        RequestSpecification spec = noAuth();
        spec = json(spec, "{\n  \"email\": \"{{user1Email}}\",\n  \"password\": \"{{password}}\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/auth/login"));
        assertStatus(r, 200);
        saveIfPresent(r, "token", "token1");
        saveIfPresent(r, "userId", "user1Id");
    }

    @Test(priority = 5, description = "Login User 2")
    public void Login_User_2() {
        RequestSpecification spec = noAuth();
        spec = json(spec, "{\n  \"email\": \"{{user2Email}}\",\n  \"password\": \"{{password}}\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/auth/login"));
        assertStatus(r, 200);
        saveIfPresent(r, "token", "token2");
        saveIfPresent(r, "userId", "user2Id");
    }

    @Test(priority = 6, description = "Login Admin")
    public void Login_Admin() {
        RequestSpecification spec = noAuth();
        spec = json(spec, "{\n  \"email\": \"{{adminEmail}}\",\n  \"password\": \"{{password}}\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/auth/login"));
        assertStatus(r, 200);
        saveIfPresent(r, "token", "adminToken");
        saveIfPresent(r, "userId", "adminId");
    }

    @Test(priority = 7, description = "NEG Register duplicate email")
    public void NEG_Register_duplicate_email() {
        RequestSpecification spec = noAuth();
        spec = json(spec, "{\n  \"name\": \"Dupe\",\n  \"email\": \"{{user1Email}}\",\n  \"password\": \"{{password}}\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/auth/register"));
        assertStatus(r, 400);
    }

    @Test(priority = 8, description = "NEG Register invalid email")
    public void NEG_Register_invalid_email() {
        RequestSpecification spec = noAuth();
        spec = json(spec, "{\n  \"name\": \"X\",\n  \"email\": \"not-an-email\",\n  \"password\": \"{{password}}\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/auth/register"));
        assertStatus(r, 400);
    }

    @Test(priority = 9, description = "NEG Register short password")
    public void NEG_Register_short_password() {
        RequestSpecification spec = noAuth();
        spec = json(spec, "{\n  \"name\": \"X\",\n  \"email\": \"shortpw_{{runId}}@campus.test\",\n  \"password\": \"123\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/auth/register"));
        assertStatus(r, 400);
    }

    @Test(priority = 10, description = "NEG Login wrong password")
    public void NEG_Login_wrong_password() {
        RequestSpecification spec = noAuth();
        spec = json(spec, "{\n  \"email\": \"{{user1Email}}\",\n  \"password\": \"wrong-password\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/auth/login"));
        assertStatus(r, 400, 401, 403, 500);
    }

    @Test(priority = 11, description = "NEG Protected route without token")
    public void NEG_Protected_route_without_token() {
        RequestSpecification spec = noAuth();
        Response r = spec.when().get(url("{{baseUrl}}/api/users/me"));
        assertStatus(r, 401, 403);
    }
}
