package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.ConnectionsPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** /connections UI. Group "authed". */
public class ConnectionsPageTest extends BaseAuthedTest {

    private ConnectionsPage page;

    @BeforeMethod(alwaysRun = true)
    public void open() { page = new ConnectionsPage(driver).openPage(); }

    @Test(groups = "authed", description = "Heading is 'Network'")
    public void heading() { assertEquals(page.heading(), "Network"); }

    @Test(groups = "authed", description = "There are at least 4 tabs")
    public void tabs() { assertTrue(page.tabCount() >= 4); }

    @Test(groups = "authed", description = "Search box is present")
    public void search() { assertTrue(page.hasSearch()); }

    @Test(groups = "authed", description = "Clicking Pending activates the Pending tab")
    public void switchTab() {
        assertTrue(page.clickTabAndWaitActive("Pending"), "Pending tab did not activate");
    }
}
