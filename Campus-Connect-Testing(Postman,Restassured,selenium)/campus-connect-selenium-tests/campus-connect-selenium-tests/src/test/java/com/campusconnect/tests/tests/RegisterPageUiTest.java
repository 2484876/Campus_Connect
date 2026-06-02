package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseTest;
import com.campusconnect.tests.pages.RegisterPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Structural / UI checks for the register page.
 * Group "smoke": needs ONLY the Angular frontend running (no backend).
 */
public class RegisterPageUiTest extends BaseTest {

    private RegisterPage register;

    @BeforeMethod(alwaysRun = true)
    public void openRegister() {
        register = new RegisterPage(driver).openPage();
    }

    @Test(groups = "smoke", description = "Register page heading is 'Join Campus Connect'")
    public void heading_isCorrect() {
        assertEquals(register.heading(), "Join Campus Connect");
    }

    @Test(groups = "smoke", description = "Name field is present")
    public void nameField_present() {
        assertTrue(register.hasNameField());
    }

    @Test(groups = "smoke", description = "Email field is present")
    public void emailField_present() {
        assertTrue(register.hasEmailField());
    }

    @Test(groups = "smoke", description = "Password field is present")
    public void passwordField_present() {
        assertTrue(register.hasPasswordField());
    }

    @Test(groups = "smoke", description = "Designation dropdown is present")
    public void roleSelect_present() {
        assertTrue(register.hasRoleSelect());
    }

    @Test(groups = "smoke", description = "Designation dropdown lists all 12 roles")
    public void roleSelect_hasTwelveOptions() {
        assertEquals(register.roleOptionCount(), 12);
    }

    @Test(groups = "smoke", description = "Default selected designation is PAT")
    public void roleSelect_defaultIsPAT() {
        assertEquals(register.selectedRoleValue(), "PROGRAMMER_ANALYST_TRAINEE");
    }

    @Test(groups = "smoke", description = "Designation can be changed to MANAGER")
    public void roleSelect_canChange() {
        register.selectRoleByValue("MANAGER");
        assertEquals(register.selectedRoleValue(), "MANAGER");
    }

    @Test(groups = "smoke", description = "Join button is present and enabled")
    public void submitButton_enabled() {
        assertTrue(register.submitEnabled());
    }

    @Test(groups = "smoke", description = "'Sign in' link back to login is present")
    public void signInLink_present() {
        assertTrue(register.hasSignInLink());
    }
}
