package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.FeedPage;
import com.campusconnect.tests.pages.GenericPage;
import com.campusconnect.tests.pages.NavBar;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Nav link navigation + logo/theme/search/logout. Group "authed". */
public class NavBarNavigationTest extends BaseAuthedTest {

    private NavBar nav;
    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() {
        new FeedPage(driver).openPage();
        nav = new NavBar(driver).waitLoaded();
        gp = new GenericPage(driver);
    }

    @DataProvider(name = "nav")
    public Object[][] nav() {
        return new Object[][] {{"/connections"},{"/chat"},{"/events"},{"/communities"},{"/notifications"},{"/feed"}};
    }

    @Test(groups = "authed", dataProvider = "nav", description = "Clicking a nav link navigates to its route")
    public void navigates(String href) {
        nav.clickLink(href);
        assertTrue(gp.urlHas(href), "expected url to contain " + href);
    }

    @Test(groups = "authed", description = "Logo returns to /feed")
    public void logoToFeed() {
        nav.clickLink("/connections");
        gp.urlHas("/connections");
        gp.clickOn(NavBar.LOGO);
        assertTrue(gp.urlHas("/feed"));
    }

    @Test(groups = "authed", description = "Theme toggle does not navigate away")
    public void themeToggleStays() {
        gp.clickOn(NavBar.THEME);
        assertTrue(driver.getCurrentUrl().contains("/feed"));
    }

    @Test(groups = "authed", description = "Search button opens the search modal")
    public void searchOpensModal() {
        gp.clickOn(NavBar.SEARCH);
        assertTrue(gp.howMany(By.cssSelector("app-search-modal")) >= 1);
    }

    @Test(groups = "authed", description = "Sign out returns to /login")
    public void logout() {
        nav.logout();
        assertTrue(gp.urlHas("/login"));
    }
}
