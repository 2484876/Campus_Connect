package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.GenericPage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Authenticated users can load each protected route (not bounced to /login). Group "authed". */
public class RouteAuthedLoadTest extends BaseAuthedTest {

    @DataProvider(name = "routes")
    public Object[][] routes() {
        return new Object[][] {
            {"/feed"},{"/connections"},{"/chat"},{"/events"},{"/communities"},
            {"/notifications"},{"/bookmarks"},{"/achievements"},{"/skills"}
        };
    }

    @Test(groups = "authed", dataProvider = "routes", description = "Protected route loads when authenticated")
    public void routeLoads(String route) {
        GenericPage gp = new GenericPage(driver).go(route);
        assertTrue(gp.urlHas(route), "expected to stay on " + route + " but was " + driver.getCurrentUrl());
        assertFalse(driver.getCurrentUrl().contains("/login"), route + " bounced to /login while authed");
    }
}
