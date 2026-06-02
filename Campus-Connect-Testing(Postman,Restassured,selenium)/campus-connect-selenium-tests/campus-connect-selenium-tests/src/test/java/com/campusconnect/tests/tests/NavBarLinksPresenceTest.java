package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.FeedPage;
import com.campusconnect.tests.pages.NavBar;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/** Each primary nav link is present. Group "authed". */
public class NavBarLinksPresenceTest extends BaseAuthedTest {

    private NavBar nav;

    @BeforeMethod(alwaysRun = true)
    public void open() { new FeedPage(driver).openPage(); nav = new NavBar(driver).waitLoaded(); }

    @DataProvider(name = "links")
    public Object[][] links() {
        return new Object[][] {{"/feed"},{"/connections"},{"/chat"},{"/events"},{"/communities"},{"/notifications"}};
    }

    @Test(groups = "authed", dataProvider = "links", description = "Nav link is present")
    public void linkPresent(String href) { assertTrue(nav.hasLink(href), "missing nav link " + href); }
}
