package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseTest;
import com.campusconnect.tests.pages.GenericPage;
import com.campusconnect.tests.pages.LoginPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Extra login-page checks. Group "smoke" (no backend). */
public class LoginExtendedTest extends BaseTest {

    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() { new LoginPage(driver).openPage(); gp = new GenericPage(driver); }

    @Test(groups = "smoke", description = "Typing into email reflects the value")
    public void emailReflects() {
        gp.typeInto(LoginPage.EMAIL, "tester@campus.edu");
        assertEquals(gp.valueOf(LoginPage.EMAIL), "tester@campus.edu");
    }

    @Test(groups = "smoke", description = "Typing into password reflects the value")
    public void passwordReflects() {
        gp.typeInto(LoginPage.PASSWORD, "Secret123");
        assertEquals(gp.valueOf(LoginPage.PASSWORD), "Secret123");
    }

    @Test(groups = "smoke", description = "Password input is masked (type=password)")
    public void passwordMasked() { assertEquals(gp.attrOf(LoginPage.PASSWORD, "type"), "password"); }

    @Test(groups = "smoke", description = "Email input is type=email")
    public void emailType() { assertEquals(gp.attrOf(LoginPage.EMAIL, "type"), "email"); }

    @Test(groups = "smoke", description = "Password placeholder is correct")
    public void passwordPlaceholder() {
        assertEquals(gp.attrOf(LoginPage.PASSWORD, "placeholder"), "Enter your password");
    }

    @Test(groups = "smoke", description = "'Join now' link points to /register")
    public void joinHref() { assertTrue(gp.attrOf(LoginPage.JOIN_LINK, "href").endsWith("/register")); }

    @Test(groups = "smoke", description = "Header logo image is present")
    public void logoPresent() { assertTrue(gp.shows(LoginPage.LOGO)); }

    @Test(groups = "smoke", description = "Subtitle text is correct")
    public void subtitle() { assertEquals(gp.textOf(LoginPage.SUBTITLE), "Connect with your campus community"); }

    @Test(groups = "smoke", description = "Enter key on empty form triggers required-field validation")
    public void enterKeySubmits() {
        gp.pressEnter(LoginPage.PASSWORD);
        assertEquals(new LoginPage(driver).errorText(), "Please fill in all fields");
    }
}
