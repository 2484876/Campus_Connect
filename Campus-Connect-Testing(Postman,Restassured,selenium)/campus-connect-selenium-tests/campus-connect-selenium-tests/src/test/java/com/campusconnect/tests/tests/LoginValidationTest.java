package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseTest;
import com.campusconnect.tests.pages.LoginPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Client-side validation on login. The component sets
 *   error = 'Please fill in all fields'
 * when email or password is empty, BEFORE any HTTP call. So these pass with
 * only the frontend running (group "smoke", no backend).
 */
public class LoginValidationTest extends BaseTest {

    private static final String REQUIRED_MSG = "Please fill in all fields";
    private LoginPage login;

    @BeforeMethod(alwaysRun = true)
    public void openLogin() {
        login = new LoginPage(driver).openPage();
    }

    @Test(groups = "smoke", description = "Submitting an empty form shows the required-fields error")
    public void emptyForm_showsError() {
        login.clickSignIn();
        assertTrue(login.isErrorDisplayed(), "Error banner should appear");
        assertEquals(login.errorText(), REQUIRED_MSG);
    }

    @Test(groups = "smoke", description = "Email only (no password) shows the required-fields error")
    public void emailOnly_showsError() {
        login.enterEmail("someone@campus.edu").clickSignIn();
        assertEquals(login.errorText(), REQUIRED_MSG);
    }

    @Test(groups = "smoke", description = "Password only (no email) shows the required-fields error")
    public void passwordOnly_showsError() {
        login.enterPassword("Test@1234").clickSignIn();
        assertEquals(login.errorText(), REQUIRED_MSG);
    }

    @Test(groups = "smoke", description = "No validation error before the user submits")
    public void noError_beforeSubmit() {
        login.enterEmail("someone@campus.edu");
        assertFalse(login.isErrorDisplayed(), "Error should not show until submit");
    }
}
