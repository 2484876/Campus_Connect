package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** /hashtag/{tag} */
public class HashtagPage extends BasePage {

    public static final By TAG_DISPLAY = By.cssSelector(".tag-display");

    public HashtagPage(WebDriver driver) { super(driver); }

    public HashtagPage openTag(String tag) { open("/hashtag/" + tag); waitVisible(TAG_DISPLAY); return this; }

    public String tagText() { return text(TAG_DISPLAY); }
}
