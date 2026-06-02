package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** /chat */
public class ChatPage extends BasePage {

    public static final By HEADING   = By.cssSelector(".convo-head h4");
    public static final By TABS      = By.cssSelector(".tabs button");
    public static final By SEARCH    = By.cssSelector("input[placeholder='Search messages...']");
    public static final By NEW_CHAT  = By.cssSelector("button.new-chat-btn");
    public static final By NO_CHAT   = By.cssSelector(".no-chat");
    public static final By CONVO_FILTER = By.cssSelector("input[placeholder='Filter conversations...']");
    public static final By NEW_CHAT_DIALOG = By.cssSelector("app-new-chat-dialog");
    public static final By ACTIVE_TAB = By.cssSelector(".tabs button.active");

    public ChatPage(WebDriver driver) { super(driver); }

    public ChatPage openPage() { open("/chat"); waitVisible(HEADING); return this; }

    public String heading()        { return text(HEADING); }
    public int tabCount()          { return count(TABS); }
    public boolean hasSearch()     { return isDisplayed(SEARCH); }
    public boolean hasNewChatBtn() { return isDisplayed(NEW_CHAT); }
    public boolean hasNoChatPlaceholder() { return isDisplayed(NO_CHAT); }
}
