package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** /bookmarks */
public class BookmarksPage extends BasePage {

    public static final By HEADING  = By.cssSelector(".page-header h2");
    public static final By SUBTITLE = By.cssSelector(".page-header .subtitle");

    public BookmarksPage(WebDriver driver) { super(driver); }

    public BookmarksPage openPage() { open("/bookmarks"); waitVisible(HEADING); return this; }

    public String heading()      { return text(HEADING); }
    public boolean hasSubtitle() { return isDisplayed(SUBTITLE); }
}
