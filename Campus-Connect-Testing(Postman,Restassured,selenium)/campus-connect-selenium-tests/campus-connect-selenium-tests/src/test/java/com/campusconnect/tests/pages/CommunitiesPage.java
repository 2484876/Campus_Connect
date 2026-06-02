package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** /communities */
public class CommunitiesPage extends BasePage {

    public static final By SIDEBAR_TITLE = By.cssSelector("h6.sidebar-title");
    public static final By TABS          = By.cssSelector(".tabs-bar button");
    public static final By SEARCH        = By.cssSelector("input[placeholder='Search communities...']");
    public static final By CREATE_TOGGLE = By.cssSelector(".community-sidebar button");
    public static final By CREATE_NAME = By.cssSelector("input[placeholder='e.g. Java Developers']");
    public static final By CREATE_PRIVATE = By.cssSelector("#privateCheck");
    public static final By ACTIVE_TAB = By.cssSelector(".tabs-bar button.active");

    public CommunitiesPage(WebDriver driver) { super(driver); }

    public CommunitiesPage openPage() { open("/communities"); waitVisible(SIDEBAR_TITLE); return this; }

    public String sidebarTitle()   { return text(SIDEBAR_TITLE); }
    public int tabCount()          { return count(TABS); }
    public boolean hasSearch()     { return isDisplayed(SEARCH); }
    public boolean hasCreateToggle(){ return isDisplayed(CREATE_TOGGLE); }
}
