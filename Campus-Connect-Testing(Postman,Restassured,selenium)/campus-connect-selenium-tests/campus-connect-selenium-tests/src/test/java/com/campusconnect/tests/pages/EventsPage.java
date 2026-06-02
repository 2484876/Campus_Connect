package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** /events */
public class EventsPage extends BasePage {

    public static final By HEADING   = By.cssSelector(".events-header h1");
    public static final By TABS      = By.cssSelector(".tabs button.tab");
    public static final By NEW_EVENT = By.cssSelector(".events-header button.btn-primary-sm");
    public static final By CHIPS     = By.cssSelector(".filter-bar .filter-chip");
    public static final By COMPOSER  = By.cssSelector("app-event-composer");
    public static final By ACTIVE_TAB = By.cssSelector(".tabs button.tab.active");

    public EventsPage(WebDriver driver) { super(driver); }

    public EventsPage openPage() { open("/events"); waitVisible(HEADING); return this; }

    public String heading()        { return text(HEADING); }
    public int tabCount()          { return count(TABS); }
    public boolean hasNewEventBtn(){ return isDisplayed(NEW_EVENT); }
    public int chipCount()         { return count(CHIPS); }
}
