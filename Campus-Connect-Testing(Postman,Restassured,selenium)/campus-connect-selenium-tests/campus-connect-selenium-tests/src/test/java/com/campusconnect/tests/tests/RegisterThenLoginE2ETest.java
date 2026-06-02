package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseTest;
import com.campusconnect.tests.pages.LoginPage;
import com.campusconnect.tests.pages.RegisterPage;
import com.campusconnect.tests.utils.TestData;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * End-to-end happy path:
 *   1. Register a brand-new user (unique email per run).
 *   2. Log in again with the SAME credentials.
 *
 * Group "e2e": REQUIRES the backend running at the apiUrl in environment.ts
 * (http://localhost:8080) in addition to the Angular frontend. On success the
 * app redirects to /feed, which is what we assert.
 *
 * Methods are ordered with priority + dependsOnMethods so the login step reuses
 * the account created by the register step.
 */
public class RegisterThenLoginE2ETest extends BaseTest {

    // Shared across the ordered methods in this class.
    private static String email;
    private static String password;
    private static String name;

    @Test(groups = "e2e", priority = 1,
          description = "Register a new user; app redirects to /feed on success")
    public void step1_registerNewUser() {
        name = TestData.defaultName();
        email = TestData.uniqueEmail();
        password = TestData.defaultPassword();

        RegisterPage register = new RegisterPage(driver).openPage();
        register.register(name, email, password);

        boolean onFeed = register.waitUrlContains("/feed");
        assertTrue(onFeed, "After registering, the app should redirect to /feed. "
                + "Current URL: " + register.currentUrl()
                + " | If an error banner showed instead, the backend at :8080 is "
                + "likely not running.");
    }

    @Test(groups = "e2e", priority = 2, dependsOnMethods = "step1_registerNewUser",
          description = "Log in with the SAME credentials just registered; redirects to /feed")
    public void step2_loginWithSameCredentials() {
        // Fresh browser (new @BeforeMethod driver). Make sure no stale session exists,
        // otherwise the login page auto-redirects logged-in users to /feed.
        driver.get(baseUrl() + "/login");
        clearSession();

        LoginPage login = new LoginPage(driver).openPage();
        login.login(email, password);

        boolean onFeed = login.waitUrlContains("/feed");
        assertTrue(onFeed, "Login with the registered credentials should redirect to /feed. "
                + "Current URL: " + login.currentUrl());
    }

    @Test(groups = "e2e", priority = 3,
          description = "Login with clearly invalid credentials shows an error and stays on /login")
    public void step3_invalidLoginShowsError() {
        driver.get(baseUrl() + "/login");
        clearSession();

        LoginPage login = new LoginPage(driver).openPage();
        login.login("no.such.user." + System.currentTimeMillis() + "@campus.edu", "WrongPass!1");

        assertTrue(login.isErrorDisplayed(), "An error banner should appear for invalid credentials");
        assertEquals(login.errorText(), "Invalid email or password");
        assertTrue(login.currentUrl().contains("/login"), "Should remain on the login page");
    }
}
