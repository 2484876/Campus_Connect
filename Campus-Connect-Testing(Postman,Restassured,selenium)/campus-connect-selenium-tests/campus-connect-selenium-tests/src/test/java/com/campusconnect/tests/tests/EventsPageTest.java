package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.EventsPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** /events UI. Group "authed". */
public class EventsPageTest extends BaseAuthedTest {

    private EventsPage page;

    @BeforeMethod(alwaysRun = true)
    public void open() { page = new EventsPage(driver).openPage(); }

    @Test(groups = "authed", description = "Heading is 'Events'")
    public void heading() { assertEquals(page.heading(), "Events"); }

    @Test(groups = "authed", description = "Four tabs (Upcoming/This week/Past/My events)")
    public void tabs() { assertEquals(page.tabCount(), 4); }

    @Test(groups = "authed", description = "New event button present")
    public void newEvent() { assertTrue(page.hasNewEventBtn()); }

    @Test(groups = "authed", description = "Category filter chips present")
    public void chips() { assertTrue(page.chipCount() >= 1); }
}
