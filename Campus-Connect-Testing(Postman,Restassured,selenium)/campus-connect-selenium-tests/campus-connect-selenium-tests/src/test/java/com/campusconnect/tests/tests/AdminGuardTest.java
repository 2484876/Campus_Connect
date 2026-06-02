package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * The test user is a regular (non-admin) account, so adminGuard must redirect
 * /admin away to /feed. Group "authed".
 */
public class AdminGuardTest extends BaseAuthedTest {

    @Test(groups = "authed", description = "Non-admin visiting /admin is redirected to /feed")
    public void nonAdmin_redirected() {
        driver.get(baseUrl() + "/admin");
        new org.openqa.selenium.support.ui.WebDriverWait(
                driver, java.time.Duration.ofSeconds(15))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/feed"));
        assertTrue(driver.getCurrentUrl().contains("/feed"));
        assertFalse(driver.getCurrentUrl().contains("/admin"));
    }
}
