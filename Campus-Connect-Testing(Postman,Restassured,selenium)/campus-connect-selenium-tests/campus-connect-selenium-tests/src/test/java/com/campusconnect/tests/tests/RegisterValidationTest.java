package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseTest;
import com.campusconnect.tests.pages.RegisterPage;
import com.campusconnect.tests.utils.TestData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Client-side validation on register. The component sets
 *   error = 'Name, email and password are required'
 * when any of name/email/password is empty, BEFORE any HTTP call.
 * Group "smoke" (no backend).
 */
public class RegisterValidationTest extends BaseTest {

    private static final String REQUIRED_MSG = "Name, email and password are required";
    private RegisterPage register;

    @BeforeMethod(alwaysRun = true)
    public void openRegister() {
        register = new RegisterPage(driver).openPage();
    }

    @Test(groups = "smoke", description = "Empty form shows the required-fields error")
    public void emptyForm_showsError() {
        register.clickJoin();
        assertTrue(register.isErrorDisplayed());
        assertEquals(register.errorText(), REQUIRED_MSG);
    }

    @Test(groups = "smoke", description = "Name only shows the required-fields error")
    public void nameOnly_showsError() {
        register.enterName(TestData.defaultName()).clickJoin();
        assertEquals(register.errorText(), REQUIRED_MSG);
    }

    @Test(groups = "smoke", description = "Name + email but no password shows the required-fields error")
    public void noPassword_showsError() {
        register.enterName(TestData.defaultName())
                .enterEmail(TestData.uniqueEmail())
                .clickJoin();
        assertEquals(register.errorText(), REQUIRED_MSG);
    }

    @Test(groups = "smoke", description = "No validation error before submitting")
    public void noError_beforeSubmit() {
        register.enterName(TestData.defaultName());
        assertFalse(register.isErrorDisplayed());
    }
}
