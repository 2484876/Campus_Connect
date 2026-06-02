package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.AuthSession;
import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.ProfilePage;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** /profile/{id} for the logged-in user. Group "authed". */
public class ProfilePageTest extends BaseAuthedTest {

    private ProfilePage profile;

    @BeforeMethod(alwaysRun = true)
    public void open() {
        String id = AuthSession.userId();
        if (id == null) {
            throw new SkipException("No userId available from session; backend may not return userId.");
        }
        profile = new ProfilePage(driver).openOwn(id);
    }

    @Test(groups = "authed", description = "Profile name is shown")
    public void name_present() { assertTrue(profile.hasName()); }

    @Test(groups = "authed", description = "Profile email is shown and looks like an email")
    public void email_present() {
        assertTrue(profile.hasEmail());
        assertTrue(profile.email().contains("@"));
    }

    @Test(groups = "authed", description = "Profile has section headers (Experience / Activity)")
    public void sections_present() { assertTrue(profile.sectionCount() >= 1); }
}
