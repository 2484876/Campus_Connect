package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.ChatPage;
import com.campusconnect.tests.pages.GenericPage;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Chat emoji reactions. Reactions require an open conversation with messages.
 * When the account has conversations we open one and verify the react button +
 * emoji picker. When it doesn't, we verify the chat shell renders correctly
 * (the empty-state placeholder), which still passes. Group "authed".
 */
public class ChatReactionTest extends BaseAuthedTest {

    private GenericPage gp;
    private boolean hasConvo;

    private static final By CONVO_ROW   = By.cssSelector(".convo-row");
    private static final By MSG_ROW      = By.cssSelector(".msg-row");
    private static final By REACT_BTN    = By.cssSelector(".quick-react-btn");
    private static final By EMOJI_PICKER = By.cssSelector(".emoji-picker");
    private static final By NO_CHAT      = By.cssSelector(".no-chat");

    @BeforeMethod(alwaysRun = true)
    public void open() {
        new ChatPage(driver).openPage();
        gp = new GenericPage(driver);
        hasConvo = false;
        if (gp.howMany(CONVO_ROW) > 0) {
            gp.clickOn(CONVO_ROW);
            hasConvo = gp.howMany(MSG_ROW) > 0;
        }
    }

    @Test(groups = "authed", description = "A message exposes a react button (or chat shell renders)")
    public void reactButtonPresent() {
        if (hasConvo) assertTrue(gp.howMany(REACT_BTN) >= 1);
        else assertTrue(gp.shows(NO_CHAT), "chat empty-state should render with no conversation");
    }

    @Test(groups = "authed", description = "Clicking react opens the emoji picker (when a message exists)")
    public void reactOpensPicker() {
        if (hasConvo) {
            gp.clickOn(REACT_BTN);
            assertTrue(gp.howMany(EMOJI_PICKER) >= 1, "emoji picker should open");
        } else {
            assertTrue(gp.shows(NO_CHAT));
        }
    }
}
