package com.campusconnect.tests.base;

import com.campusconnect.tests.config.Config;
import com.campusconnect.tests.pages.LoginPage;
import com.campusconnect.tests.pages.RegisterPage;
import com.campusconnect.tests.utils.TestData;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Provides an authenticated session to the authed-page tests.
 *
 * Two modes:
 *  1. SEEDED ACCOUNT (preferred for data-dependent tests): if you pass
 *       -Dtest.email=... -Dtest.password=...
 *     the first call LOGS IN as that existing account through the UI. Use an
 *     account that already has posts / connections / conversations so the
 *     like/comment/kudos/chat-reaction tests have data to act on (and pass
 *     instead of skip).
 *  2. FRESH ACCOUNT (default): the first call REGISTERS a brand-new user. Good
 *     for structural tests, but data-dependent tests will skip (empty feed/chat).
 *
 * Either way the token + user JSON are cached and injected into every later
 * browser, so the route guard lets tests straight onto any protected page.
 *
 * REQUIRES the backend running.
 */
public final class AuthSession {

    private static String token;
    private static String userJson;
    private static String userId;
    private static boolean populated = false;

    private AuthSession() { }

    private static String seedEmail() {
        String v = System.getProperty("test.email");
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static String seedPassword() {
        String v = System.getProperty("test.password");
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    /** True if the suite is running against a seeded (pre-existing) account. */
    public static boolean usingSeededAccount() {
        return seedEmail() != null && seedPassword() != null;
    }

    /** Guarantees the given driver has a valid session in sessionStorage. */
    public static synchronized void ensureLoggedIn(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        if (!populated) {
            if (usingSeededAccount()) {
                LoginPage login = new LoginPage(driver).openPage();
                login.login(seedEmail(), seedPassword());
            } else {
                RegisterPage reg = new RegisterPage(driver).openPage();
                reg.register(TestData.defaultName(), TestData.uniqueEmail(), TestData.defaultPassword());
            }
            new WebDriverWait(driver, Config.explicitWait())
                    .until(ExpectedConditions.urlContains("/feed"));

            token = (String) js.executeScript("return window.sessionStorage.getItem('token');");
            userJson = (String) js.executeScript("return window.sessionStorage.getItem('user');");
            try {
                Object id = js.executeScript(
                        "return JSON.parse(window.sessionStorage.getItem('user')).userId;");
                userId = id == null ? null : String.valueOf(id);
            } catch (Exception e) {
                userId = null;
            }
            if (token != null) {
                populated = true;
            }
            return; // already logged in, sitting on /feed
        }

        // Inject the cached session into this fresh browser.
        driver.get(Config.baseUrl() + "/login");
        js.executeScript(
                "window.sessionStorage.setItem('token', arguments[0]);"
              + "window.sessionStorage.setItem('user', arguments[1]);",
                token, userJson);
    }

    /** userId of the test account (may be null if the backend omitted it). */
    public static String userId() {
        return userId;
    }
}
