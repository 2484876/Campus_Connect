package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.EventsPage;
import com.campusconnect.tests.pages.GenericPage;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Events tab switching + composer + chips. Group "authed". */
public class EventsTabsTest extends BaseAuthedTest {

    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() { new EventsPage(driver).openPage(); gp = new GenericPage(driver); }

    @DataProvider(name = "tabs")
    public Object[][] tabs() {
        return new Object[][] {{"Upcoming"},{"This week"},{"Past"},{"My events"}};
    }

    @Test(groups = "authed", dataProvider = "tabs", description = "Clicking an events tab activates it")
    public void tabActivates(String label) {
        assertTrue(gp.clickTabAndWaitActive("tabs", label),
                "tab did not become active: " + label);
    }

    @Test(groups = "authed", description = "New event button opens the composer")
    public void composerOpens() {
        gp.clickOn(EventsPage.NEW_EVENT);
        assertTrue(gp.howMany(EventsPage.COMPOSER) >= 1);
    }

    @Test(groups = "authed", description = "At least one category filter chip is present")
    public void chipsPresent() { assertTrue(gp.howMany(EventsPage.CHIPS) >= 1); }
}
