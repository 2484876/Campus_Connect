package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.CommunitiesPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** /communities UI. Group "authed". */
public class CommunitiesPageTest extends BaseAuthedTest {

    private CommunitiesPage page;

    @BeforeMethod(alwaysRun = true)
    public void open() { page = new CommunitiesPage(driver).openPage(); }

    @Test(groups = "authed", description = "Sidebar shows 'Your Communities'")
    public void sidebar() { assertEquals(page.sidebarTitle(), "Your Communities"); }

    @Test(groups = "authed", description = "Discover / My Communities tabs present")
    public void tabs() { assertTrue(page.tabCount() >= 2); }

    @Test(groups = "authed", description = "Community search input present")
    public void search() { assertTrue(page.hasSearch()); }

    @Test(groups = "authed", description = "Create community toggle present")
    public void createToggle() { assertTrue(page.hasCreateToggle()); }
}
