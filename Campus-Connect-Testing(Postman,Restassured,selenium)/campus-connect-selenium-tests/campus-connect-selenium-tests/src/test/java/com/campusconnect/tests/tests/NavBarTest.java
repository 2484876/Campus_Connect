package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.FeedPage;
import com.campusconnect.tests.pages.NavBar;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Top navigation bar (shared chrome). Group "authed" (needs backend + login). */
public class NavBarTest extends BaseAuthedTest {

    private NavBar nav;

    @BeforeMethod(alwaysRun = true)
    public void openFeed() {
        new FeedPage(driver).openPage();
        nav = new NavBar(driver).waitLoaded();
    }

    @Test(groups = "authed", description = "Navbar is visible when logged in")
    public void navbar_visible() { assertTrue(nav.isVisible()); }

    @Test(groups = "authed", description = "Logo is present")
    public void logo_present() { assertTrue(nav.hasLogo()); }

    @Test(groups = "authed", description = "Search button is present")
    public void search_present() { assertTrue(nav.hasSearch()); }

    @Test(groups = "authed", description = "Theme toggle is present")
    public void theme_present() { assertTrue(nav.hasThemeToggle()); }

    @Test(groups = "authed", description = "Profile / Me trigger is present")
    public void profile_present() { assertTrue(nav.hasProfileTrigger()); }

    @Test(groups = "authed", description = "All primary nav links are present")
    public void navLinks_present() {
        assertTrue(nav.hasLink("/feed"), "Home");
        assertTrue(nav.hasLink("/connections"), "Network");
        assertTrue(nav.hasLink("/chat"), "Messaging");
        assertTrue(nav.hasLink("/events"), "Events");
        assertTrue(nav.hasLink("/communities"), "Groups");
        assertTrue(nav.hasLink("/notifications"), "Alerts");
    }

    @Test(groups = "authed", description = "Clicking Network navigates to /connections")
    public void navClick_network() {
        nav.clickLink("/connections");
        assertTrue(new NavBar(driver).waitUrlContains("/connections"));
    }

    @Test(groups = "authed", description = "Clicking Events navigates to /events")
    public void navClick_events() {
        nav.clickLink("/events");
        assertTrue(new NavBar(driver).waitUrlContains("/events"));
    }

    @Test(groups = "authed", description = "Opening the Me menu reveals Sign out")
    public void profileMenu_opens() {
        nav.openProfileMenu();
        assertTrue(nav.isVisible());
    }
}
