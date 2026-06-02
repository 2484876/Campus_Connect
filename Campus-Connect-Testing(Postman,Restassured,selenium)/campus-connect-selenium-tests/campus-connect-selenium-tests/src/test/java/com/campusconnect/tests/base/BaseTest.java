package com.campusconnect.tests.base;

import com.campusconnect.tests.config.Config;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/**
 * Base for all test classes. Creates a fresh browser per test method so tests
 * are independent (a failure in one cannot leak session state into the next).
 */
public abstract class BaseTest {

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.create();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Clears sessionStorage/localStorage + cookies so the auth guard does not auto-redirect. */
    protected void clearSession() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "window.sessionStorage.clear(); window.localStorage.clear();");
            driver.manage().deleteAllCookies();
        } catch (Exception ignored) {
            // storage may be inaccessible before any page is loaded; safe to ignore
        }
    }

    protected String baseUrl() {
        return Config.baseUrl();
    }

    /** Exposed so the reporting listener can grab a screenshot on failure. */
    public WebDriver getDriver() {
        return driver;
    }
}
