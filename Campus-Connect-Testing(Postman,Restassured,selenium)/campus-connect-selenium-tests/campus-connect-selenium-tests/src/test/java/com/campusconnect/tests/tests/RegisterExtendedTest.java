package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseTest;
import com.campusconnect.tests.pages.GenericPage;
import com.campusconnect.tests.pages.RegisterPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Extra register-page checks. Group "smoke" (no backend). */
public class RegisterExtendedTest extends BaseTest {

    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() { new RegisterPage(driver).openPage(); gp = new GenericPage(driver); }

    @Test(groups = "smoke", description = "Password input is masked")
    public void passwordMasked() { assertEquals(gp.attrOf(RegisterPage.PASSWORD, "type"), "password"); }

    @Test(groups = "smoke", description = "Email input is type=email")
    public void emailType() { assertEquals(gp.attrOf(RegisterPage.EMAIL, "type"), "email"); }

    @Test(groups = "smoke", description = "'Sign in' link points to /login")
    public void signInHref() { assertTrue(gp.attrOf(RegisterPage.SIGNIN_LINK, "href").endsWith("/login")); }

    @Test(groups = "smoke", description = "Header logo image is present")
    public void logoPresent() { assertTrue(gp.shows(RegisterPage.LOGO)); }

    @Test(groups = "smoke", description = "Subtitle text is correct")
    public void subtitle() { assertEquals(gp.textOf(RegisterPage.SUBTITLE), "Make the most of your professional journey"); }

    @Test(groups = "smoke", description = "There are at least 5 form groups")
    public void formGroups() { assertTrue(gp.howMany(RegisterPage.FORM_GROUPS) >= 5); }

    @Test(groups = "smoke", description = "Typing into name reflects the value")
    public void nameReflects() {
        gp.typeInto(RegisterPage.NAME, "QA Person");
        assertEquals(gp.valueOf(RegisterPage.NAME), "QA Person");
    }

    @Test(groups = "smoke", description = "Department field is present (optional)")
    public void departmentPresent() { assertTrue(gp.shows(RegisterPage.DEPARTMENT)); }

    @Test(groups = "smoke", description = "Position field is present (optional)")
    public void positionPresent() { assertTrue(gp.shows(RegisterPage.POSITION)); }
}
