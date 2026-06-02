package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** App entry behaviour without a session. Group "smoke". */
public class AppRootTest extends BaseTest {

    @Test(groups = "smoke", description = "Root path redirects an anonymous user to /login")
    public void rootRedirectsToLogin() {
        driver.get(baseUrl() + "/");
        clearSession();
        driver.get(baseUrl() + "/");
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test(groups = "smoke", description = "Login page has a non-empty document title")
    public void loginTitle() {
        driver.get(baseUrl() + "/login");
        assertFalse(driver.getTitle().isBlank());
    }

    @Test(groups = "smoke", description = "Unknown route redirects (wildcard route)")
    public void unknownRouteRedirects() {
        driver.get(baseUrl() + "/this-route-does-not-exist");
        clearSession();
        driver.get(baseUrl() + "/this-route-does-not-exist");
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }
}
