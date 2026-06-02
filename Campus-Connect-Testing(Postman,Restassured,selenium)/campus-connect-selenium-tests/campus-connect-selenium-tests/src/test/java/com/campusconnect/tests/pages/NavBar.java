package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * The top navigation bar shown on every authenticated page
 * (rendered when auth.isLoggedIn() is true).
 * Angular renders routerLink="/x" as href="/x", so we locate by href.
 */
public class NavBar extends BasePage {

    public static final By NAV       = By.cssSelector("nav.navbar-main");
    public static final By LOGO      = By.cssSelector("a.logo");
    public static final By SEARCH    = By.cssSelector("button.search-box");
    public static final By THEME     = By.cssSelector("button.theme-toggle");
    public static final By PROFILE   = By.cssSelector(".profile-trigger");
    public static final By DROPDOWN  = By.cssSelector(".profile-dropdown");
    public static final By LOGOUT    = By.cssSelector(".profile-dropdown a.text-danger");

    public static By navLink(String href) {
        return By.cssSelector("a.nav-item[href='" + href + "']");
    }

    public NavBar(WebDriver driver) {
        super(driver);
    }

    public NavBar waitLoaded() {
        waitVisible(NAV);
        return this;
    }

    public boolean isVisible()              { return isDisplayed(NAV); }
    public boolean hasLogo()                { return isDisplayed(LOGO); }
    public boolean hasSearch()              { return isDisplayed(SEARCH); }
    public boolean hasThemeToggle()         { return isDisplayed(THEME); }
    public boolean hasProfileTrigger()      { return isDisplayed(PROFILE); }
    public boolean hasLink(String href)     { return isDisplayed(navLink(href)); }

    public void clickLink(String href)      { click(navLink(href)); }

    public NavBar openProfileMenu() {
        click(PROFILE);
        waitVisible(DROPDOWN);
        return this;
    }

    public void logout() {
        openProfileMenu();
        click(LOGOUT);
    }
}
