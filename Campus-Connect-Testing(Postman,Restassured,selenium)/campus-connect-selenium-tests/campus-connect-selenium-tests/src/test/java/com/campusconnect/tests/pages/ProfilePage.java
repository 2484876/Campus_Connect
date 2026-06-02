package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** /profile/{id} */
public class ProfilePage extends BasePage {

    public static final By NAME    = By.cssSelector(".profile-info h2");
    public static final By EMAIL   = By.cssSelector(".profile-email");
    public static final By CARD    = By.cssSelector(".profile-card");
    public static final By SECTION = By.cssSelector("h5.section-h");
    public static final By KEBAB   = By.cssSelector(".kebab-btn");
    public static final By EDIT_ITEM = By.xpath("//button[contains(@class,'kebab-item')][contains(.,'Edit profile')]");
    public static final By EDIT_NAME = By.xpath("//div[contains(@class,'card')]//label[contains(.,'Name')]/following-sibling::input");
    public static final By SKILLS  = By.cssSelector(".profile-skills");
    public static final By KUDOS_TRIGGER = By.cssSelector("button.kudos-trigger");
    public static final By ENDORSE_WIDGET = By.cssSelector("app-endorsements-widget");
    public static final By ENDORSE_TITLE  = By.cssSelector("app-endorsements-widget .head-title");

    public ProfilePage(WebDriver driver) { super(driver); }

    public ProfilePage openOwn(String userId) { open("/profile/" + userId); waitVisible(CARD); return this; }

    public boolean hasName()     { return isDisplayed(NAME); }
    public String name()         { return text(NAME); }
    public boolean hasEmail()    { return isDisplayed(EMAIL); }
    public String email()        { return text(EMAIL); }
    public int sectionCount()    { return count(SECTION); }
}
