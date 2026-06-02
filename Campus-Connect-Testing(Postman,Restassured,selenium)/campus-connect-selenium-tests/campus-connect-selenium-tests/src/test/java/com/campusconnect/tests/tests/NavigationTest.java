package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseTest;
import com.campusconnect.tests.pages.LoginPage;
import com.campusconnect.tests.pages.RegisterPage;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Routing between the two auth pages via the on-page links.
 * Group "smoke" (no backend).
 */
public class NavigationTest extends BaseTest {

    @Test(groups = "smoke", description = "'Join now' on login navigates to /register")
    public void login_to_register() {
        LoginPage login = new LoginPage(driver).openPage();
        RegisterPage register = login.goToRegister();
        assertTrue(register.currentUrl().contains("/register"),
                "URL should contain /register but was: " + register.currentUrl());
        assertEquals(register.heading(), "Join Campus Connect");
    }

    @Test(groups = "smoke", description = "'Sign in' on register navigates to /login")
    public void register_to_login() {
        RegisterPage register = new RegisterPage(driver).openPage();
        LoginPage login = register.goToLogin();
        assertTrue(login.currentUrl().contains("/login"),
                "URL should contain /login but was: " + login.currentUrl());
        assertEquals(login.heading(), "Sign in");
    }

    @Test(groups = "smoke", description = "Round trip login -> register -> login works")
    public void round_trip() {
        LoginPage login = new LoginPage(driver).openPage();
        RegisterPage register = login.goToRegister();
        LoginPage back = register.goToLogin();
        assertTrue(back.currentUrl().contains("/login"));
        assertEquals(back.heading(), "Sign in");
    }
}
