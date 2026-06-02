package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.FeedPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** /feed UI. Group "authed". */
public class FeedPageTest extends BaseAuthedTest {

    private FeedPage feed;

    @BeforeMethod(alwaysRun = true)
    public void open() { feed = new FeedPage(driver).openPage(); }

    @Test(groups = "authed", description = "Create-post input is present")
    public void postInput_present() { assertTrue(feed.hasPostInput()); }

    @Test(groups = "authed", description = "Post button is present")
    public void postButton_present() { assertTrue(feed.hasPostButton()); }

    @Test(groups = "authed", description = "Feed has two tabs (All / For you)")
    public void feedTabs_two() { assertEquals(feed.feedTabCount(), 2); }

    @Test(groups = "authed", description = "Left profile card is present")
    public void profileCard_present() { assertTrue(feed.hasProfileCard()); }

    @Test(groups = "authed", description = "Media buttons (Photo/Video/Poll) are present")
    public void mediaButtons_present() { assertTrue(feed.mediaButtonCount() >= 2); }
}
