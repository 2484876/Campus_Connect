package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.ChatPage;
import com.campusconnect.tests.pages.GenericPage;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Chat message composer. The composer appears once a conversation is open.
 * If there are conversations we open one and exercise the input; otherwise we
 * verify the empty-state shell renders. No skips. Group "authed".
 */
public class ChatMessageInputTest extends BaseAuthedTest {

    private GenericPage gp;
    private boolean hasConvo;

    private static final By CONVO_ROW = By.cssSelector(".convo-row");
    private static final By MSG_INPUT = By.cssSelector("input.msg-input");
    private static final By NO_CHAT   = By.cssSelector(".no-chat");

    @BeforeMethod(alwaysRun = true)
    public void open() {
        new ChatPage(driver).openPage();
        gp = new GenericPage(driver);
        hasConvo = false;
        if (gp.howMany(CONVO_ROW) > 0) {
            gp.clickOn(CONVO_ROW);
            hasConvo = gp.howMany(MSG_INPUT) > 0;
        }
    }

    @Test(groups = "authed", description = "Composer input present (or empty-state renders)")
    public void composerPresent() {
        if (hasConvo) assertTrue(gp.howMany(MSG_INPUT) >= 1);
        else assertTrue(gp.shows(NO_CHAT));
    }

    @Test(groups = "authed", description = "Typing reflects in the composer (when open)")
    public void composerTyping() {
        if (hasConvo) {
            gp.typeInto(MSG_INPUT, "hello there");
            assertEquals(gp.valueOf(MSG_INPUT), "hello there");
        } else {
            assertTrue(gp.shows(NO_CHAT));
        }
    }
}
