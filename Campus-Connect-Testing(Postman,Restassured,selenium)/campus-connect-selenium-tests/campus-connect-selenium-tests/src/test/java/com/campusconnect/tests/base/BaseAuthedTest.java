package com.campusconnect.tests.base;

import org.testng.annotations.BeforeMethod;

/**
 * Base for tests that need an authenticated session. Runs AFTER BaseTest.setUp
 * (TestNG executes superclass @BeforeMethod first), so the driver already exists.
 * Establishes a logged-in session via AuthSession (needs the backend).
 */
public abstract class BaseAuthedTest extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void logIn() {
        AuthSession.ensureLoggedIn(driver);
    }
}
