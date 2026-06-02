package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.FeedPage;
import com.campusconnect.tests.pages.GenericPage;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Feed composer + tabs behaviour. Group "authed". */
public class FeedExtendedTest extends BaseAuthedTest {

    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() { new FeedPage(driver).openPage(); gp = new GenericPage(driver); }

    @Test(groups = "authed", description = "Post button is disabled when the composer is empty")
    public void postDisabledWhenEmpty() { assertFalse(gp.enabled(FeedPage.POST_BTN)); }

    @Test(groups = "authed", description = "Typing in the composer enables the Post button")
    public void typingEnablesPost() {
        gp.typeInto(FeedPage.POST_INPUT, "hello from selenium automation");
        assertTrue(gp.enabled(FeedPage.POST_BTN));
    }

    @Test(groups = "authed", description = "Post button label is 'Post'")
    public void postLabel() { assertEquals(gp.textOf(FeedPage.POST_BTN), "Post"); }

    @Test(groups = "authed", description = "Clicking Poll opens the poll creator")
    public void pollOpens() {
        gp.clickOn(By.xpath("//button[contains(@class,'media-btn')][.//span[normalize-space()='Poll']]"));
        assertTrue(gp.shows(FeedPage.POLL_CREATOR));
    }

    @Test(groups = "authed", description = "The All feed tab is active by default")
    public void allTabActive() { assertTrue(gp.textOf(FeedPage.ACTIVE_TAB).contains("All")); }

    @Test(groups = "authed", description = "Clicking For you activates that tab")
    public void forYouActivates() {
        gp.clickOn(By.xpath("//button[contains(@class,'feed-tab')][contains(.,'For you')]"));
        assertTrue(gp.textOf(FeedPage.ACTIVE_TAB).contains("For you"));
    }

    @Test(groups = "authed", description = "Profile card shows the current user's name")
    public void profileCardName() { assertTrue(gp.shows(By.cssSelector(".profile-card h6"))); }

    @Test(groups = "authed", description = "Celebrants widget is present in the right column")
    public void celebrantsWidget() { assertTrue(gp.howMany(FeedPage.RIGHT_CELEBRANTS) >= 1); }
}
