package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.FeedPage;
import com.campusconnect.tests.pages.GenericPage;
import com.campusconnect.tests.pages.NavBar;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/** Profile dropdown menu items. Group "authed". */
public class NavBarDropdownTest extends BaseAuthedTest {

    private GenericPage gp;
    private NavBar nav;

    @BeforeMethod(alwaysRun = true)
    public void open() {
        new FeedPage(driver).openPage();
        nav = new NavBar(driver).waitLoaded();
        nav.openProfileMenu();
        gp = new GenericPage(driver);
    }

    @Test(groups = "authed", description = "Dropdown shows Sign out")
    public void signOut() { assertTrue(gp.shows(NavBar.LOGOUT)); }

    @Test(groups = "authed", description = "Dropdown shows Saved posts")
    public void savedPosts() {
        assertTrue(gp.shows(By.cssSelector(".profile-dropdown a[href='/bookmarks']")));
    }

    @Test(groups = "authed", description = "Dropdown shows View profile")
    public void viewProfile() {
        assertTrue(gp.shows(By.xpath("//div[contains(@class,'profile-dropdown')]//a[contains(.,'View profile')]")));
    }
}
