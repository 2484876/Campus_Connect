package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseTest;
import com.campusconnect.tests.pages.LoginPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Structural / UI checks for the login page.
 * Group "smoke": needs ONLY the Angular frontend running (no backend).
 */
public class LoginPageUiTest extends BaseTest {

    private LoginPage login;

    @BeforeMethod(alwaysRun = true)
    public void openLogin() {
        login = new LoginPage(driver).openPage();
    }

    @Test(groups = "smoke", description = "Login page shows the 'Sign in' heading")
    public void heading_isSignIn() {
        assertEquals(login.heading(), "Sign in");
    }

    @Test(groups = "smoke", description = "Email field is present")
    public void emailField_present() {
        assertTrue(login.hasEmailField(), "Email field should be visible");
    }

    @Test(groups = "smoke", description = "Password field is present")
    public void passwordField_present() {
        assertTrue(login.hasPasswordField(), "Password field should be visible");
    }

    @Test(groups = "smoke", description = "Password field masks input (type=password)")
    public void passwordField_isMasked() {
        assertEquals(login.passwordType(), "password");
    }

    @Test(groups = "smoke", description = "Email field has the expected placeholder")
    public void emailField_placeholder() {
        assertEquals(login.emailPlaceholder(), "you@campus.edu");
    }

    @Test(groups = "smoke", description = "Sign in button is present and enabled")
    public void submitButton_enabled() {
        assertTrue(login.submitEnabled(), "Sign in button should be enabled");
    }

    @Test(groups = "smoke", description = "Sign in button label is correct")
    public void submitButton_label() {
        assertEquals(login.submitText(), "Sign in");
    }

    @Test(groups = "smoke", description = "'Join now' link to register is present")
    public void joinLink_present() {
        assertTrue(login.hasJoinLink(), "'Join now' link should be visible");
    }

    @Test(groups = "smoke", description = "No error banner is shown on a fresh load")
    public void noError_onFreshLoad() {
        assertFalse(login.isErrorDisplayed(), "No error should show before submitting");
    }
}
