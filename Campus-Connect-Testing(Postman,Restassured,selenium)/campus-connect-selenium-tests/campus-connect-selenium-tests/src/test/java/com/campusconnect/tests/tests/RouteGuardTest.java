package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Protected routes must bounce an UNAUTHENTICATED visitor to /login.
 * This is the authGuard running client-side, so it needs ONLY the frontend.
 * Group "smoke".
 */
public class RouteGuardTest extends BaseTest {

    @DataProvider(name = "protectedRoutes")
    public Object[][] protectedRoutes() {
        return new Object[][] {
            {"/feed"}, {"/connections"}, {"/chat"}, {"/events"},
            {"/communities"}, {"/notifications"}, {"/bookmarks"},
            {"/achievements"}, {"/skills"}
        };
    }

    @Test(groups = "smoke", dataProvider = "protectedRoutes",
          description = "Unauthenticated access to a protected route redirects to /login")
    public void protectedRoute_redirectsToLogin(String route) {
        driver.get(baseUrl() + route);
        clearSession();                 // ensure no token
        driver.get(baseUrl() + route);  // try again with a clean session

        new org.openqa.selenium.support.ui.WebDriverWait(
                driver, java.time.Duration.ofSeconds(15))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/login"));

        assertTrue(driver.getCurrentUrl().contains("/login"),
                route + " should redirect to /login when not authenticated, but URL was "
                        + driver.getCurrentUrl());
    }
}
