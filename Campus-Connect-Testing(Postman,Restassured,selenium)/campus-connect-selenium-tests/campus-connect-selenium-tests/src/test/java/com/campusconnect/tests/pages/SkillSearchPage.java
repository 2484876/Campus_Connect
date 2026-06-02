package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** /skills */
public class SkillSearchPage extends BasePage {

    public static final By HEADING    = By.cssSelector(".page-head h1");
    public static final By SEARCH     = By.cssSelector("input.search-input");
    public static final By SEARCH_BTN = By.cssSelector(".search-box button.btn-primary-sm");
    public static final By TRENDING = By.cssSelector(".trending-section");

    public SkillSearchPage(WebDriver driver) { super(driver); }

    public SkillSearchPage openPage() { open("/skills"); waitVisible(HEADING); return this; }

    public String heading()        { return text(HEADING); }
    public boolean hasSearch()     { return isDisplayed(SEARCH); }
    public boolean hasSearchBtn()  { return isDisplayed(SEARCH_BTN); }
}
