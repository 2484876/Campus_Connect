package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** /feed */
public class FeedPage extends BasePage {

    public static final By POST_INPUT  = By.cssSelector("textarea.post-input");
    public static final By POST_BTN     = By.cssSelector("button.btn-primary-sm");
    public static final By FEED_TABS    = By.cssSelector("button.feed-tab");
    public static final By PROFILE_CARD = By.cssSelector(".profile-card");
    public static final By MEDIA_BTNS   = By.cssSelector(".media-btn");
    public static final By LEFT_NAV_LINKS = By.cssSelector(".profile-links a");
    public static final By POLL_CREATOR   = By.cssSelector("app-poll-creator");
    public static final By POLL_BTN       = By.xpath("//button[contains(@class,'media-btn')][.//span[contains(.,'Poll')] or contains(.,'Poll')]");
    public static final By ACTIVE_TAB     = By.cssSelector("button.feed-tab.active");
    public static final By RIGHT_CELEBRANTS = By.cssSelector("app-celebrants-widget");
    public static final By POST_CARD     = By.cssSelector(".post-card, app-post, .feed-post");
    public static final By LIKE_BTN      = By.xpath("(//div[contains(@class,'post-actions')]//button[contains(@class,'action-btn')][.//span[normalize-space()='Like' or normalize-space()='Liked']])[1]");
    public static final By LIKE_LABEL    = By.xpath("(//div[contains(@class,'post-actions')]//button[contains(@class,'action-btn')]//span[normalize-space()='Like' or normalize-space()='Liked'])[1]");
    public static final By COMMENT_BTN   = By.xpath("(//div[contains(@class,'post-actions')]//button[contains(@class,'action-btn')][.//span[normalize-space()='Comment']])[1]");
    public static final By COMMENT_INPUT = By.cssSelector("input.comment-input");
    public static final By PYMK_PROFILE_LINK = By.cssSelector("app-people-you-may-know a.pymk-name");
    public static final By TRENDING_HASHTAG  = By.cssSelector("app-trending-sidebar a, .trending a");

    public FeedPage(WebDriver driver) { super(driver); }

    public FeedPage openPage() { open("/feed"); waitVisible(POST_INPUT); return this; }

    /** Creates a post with the given text and waits for the composer to clear. */
    public FeedPage createPost(String body) {
        type(POST_INPUT, body);
        click(POST_BTN);
        // composer clears (newPost reset) after a successful post
        wait.until(d -> {
            try {
                String v = d.findElement(POST_INPUT).getAttribute("value");
                return v == null || v.isEmpty();
            } catch (Exception e) {
                return false;
            }
        });
        return this;
    }

    public boolean hasPostInput()   { return isDisplayed(POST_INPUT); }
    public boolean hasPostButton()  { return isDisplayed(POST_BTN); }
    public int feedTabCount()       { return count(FEED_TABS); }
    public boolean hasProfileCard() { return isDisplayed(PROFILE_CARD); }
    public int mediaButtonCount()   { return count(MEDIA_BTNS); }
}
