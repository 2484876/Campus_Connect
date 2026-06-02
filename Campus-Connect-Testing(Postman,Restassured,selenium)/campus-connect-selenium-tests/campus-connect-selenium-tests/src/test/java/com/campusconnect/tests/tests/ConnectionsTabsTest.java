package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.ConnectionsPage;
import com.campusconnect.tests.pages.GenericPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Connections tab switching + search typing. Group "authed". */
public class ConnectionsTabsTest extends BaseAuthedTest {

    private ConnectionsPage page;

    @BeforeMethod(alwaysRun = true)
    public void open() { page = new ConnectionsPage(driver).openPage(); }

    @DataProvider(name = "tabs")
    public Object[][] tabs() {
        return new Object[][] {{"Connections"},{"Pending"},{"Sent"},{"Suggestions"}};
    }

    @Test(groups = "authed", dataProvider = "tabs", description = "Clicking a tab activates it")
    public void tabActivates(String label) {
        assertTrue(page.clickTabAndWaitActive(label), "tab not active: " + label);
    }

    @Test(groups = "authed", description = "Search box accepts input")
    public void searchTyping() {
        GenericPage gp = new GenericPage(driver);
        gp.typeInto(ConnectionsPage.SEARCH, "Rahul");
        assertEquals(gp.valueOf(ConnectionsPage.SEARCH), "Rahul");
    }
}
