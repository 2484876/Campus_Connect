package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.CommunitiesPage;
import com.campusconnect.tests.pages.GenericPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Communities create form + tabs. Group "authed". */
public class CommunitiesExtendedTest extends BaseAuthedTest {

    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() { new CommunitiesPage(driver).openPage(); gp = new GenericPage(driver); }

    @Test(groups = "authed", description = "Create toggle reveals the community name field")
    public void createShowsName() {
        gp.clickOn(CommunitiesPage.CREATE_TOGGLE);
        assertTrue(gp.shows(CommunitiesPage.CREATE_NAME));
    }

    @Test(groups = "authed", description = "Create form has a Private checkbox")
    public void createShowsPrivate() {
        gp.clickOn(CommunitiesPage.CREATE_TOGGLE);
        assertTrue(gp.shows(CommunitiesPage.CREATE_PRIVATE));
    }

    @Test(groups = "authed", description = "Community search accepts input")
    public void searchTyping() {
        gp.typeInto(CommunitiesPage.SEARCH, "Java");
        assertEquals(gp.valueOf(CommunitiesPage.SEARCH), "Java");
    }

    @Test(groups = "authed", description = "Discover tab activates")
    public void discoverActive() {
        assertTrue(gp.clickTabAndWaitActive("tabs-bar", "Discover"),
                "Discover tab did not activate");
    }

    @Test(groups = "authed", description = "My Communities tab activates")
    public void myActive() {
        assertTrue(gp.clickTabAndWaitActive("tabs-bar", "My Communities"),
                "My Communities tab did not activate");
    }
}
