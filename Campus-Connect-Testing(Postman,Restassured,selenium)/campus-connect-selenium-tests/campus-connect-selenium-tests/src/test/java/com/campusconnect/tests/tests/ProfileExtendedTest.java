package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.AuthSession;
import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.GenericPage;
import com.campusconnect.tests.pages.ProfilePage;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Own-profile deeper checks. Group "authed". */
public class ProfileExtendedTest extends BaseAuthedTest {

    private ProfilePage profile;
    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() {
        String id = AuthSession.userId();
        if (id == null) throw new SkipException("No userId from session");
        profile = new ProfilePage(driver).openOwn(id);
        gp = new GenericPage(driver);
    }

    @Test(groups = "authed", description = "Profile shows a name")
    public void name() { assertTrue(profile.hasName()); }

    @Test(groups = "authed", description = "Profile email looks like an email")
    public void email() { assertTrue(profile.email().contains("@")); }

    @Test(groups = "authed", description = "Profile has section headers")
    public void sections() { assertTrue(profile.sectionCount() >= 1); }

    @Test(groups = "authed", description = "Edit profile opens an editable name field")
    public void editOpens() {
        gp.clickOn(ProfilePage.KEBAB);
        gp.clickOn(ProfilePage.EDIT_ITEM);
        assertTrue(gp.shows(ProfilePage.EDIT_NAME));
    }
}
