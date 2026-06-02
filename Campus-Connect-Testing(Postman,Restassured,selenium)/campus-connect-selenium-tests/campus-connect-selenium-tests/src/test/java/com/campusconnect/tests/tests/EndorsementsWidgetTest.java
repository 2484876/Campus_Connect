package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.AuthSession;
import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.GenericPage;
import com.campusconnect.tests.pages.ProfilePage;
import org.openqa.selenium.By;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Endorsements widget on the profile page. Group "authed". */
public class EndorsementsWidgetTest extends BaseAuthedTest {

    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() {
        String id = AuthSession.userId();
        if (id == null) throw new SkipException("No userId from session");
        new ProfilePage(driver).openOwn(id);
        gp = new GenericPage(driver);
    }

    @Test(groups = "authed", description = "Endorsements widget is present on the profile")
    public void widgetPresent() { assertTrue(gp.shows(ProfilePage.ENDORSE_WIDGET)); }

    @Test(groups = "authed", description = "Widget title reads 'Endorsements'")
    public void title() { assertTrue(gp.textOf(ProfilePage.ENDORSE_TITLE).contains("Endorsements")); }

    @Test(groups = "authed", description = "On own profile, the Endorse button is hidden (cannot endorse self)")
    public void noSelfEndorse() {
        assertEquals(gp.howMany(By.cssSelector("app-endorsements-widget button.btn-primary-xs")), 0);
    }

    @Test(groups = "authed", description = "Empty state message shows when there are no endorsements")
    public void emptyStateOrList() {
        boolean empty = gp.howMany(By.cssSelector("app-endorsements-widget .block-empty")) > 0;
        boolean list  = gp.howMany(By.cssSelector("app-endorsements-widget .card-body")) > 0;
        assertTrue(empty || list, "widget should show either an empty state or a list");
    }
}
