package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.ChatPage;
import com.campusconnect.tests.pages.GenericPage;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Chat tabs + filter + new-chat dialog. Group "authed". */
public class ChatExtendedTest extends BaseAuthedTest {

    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() { new ChatPage(driver).openPage(); gp = new GenericPage(driver); }

    @DataProvider(name = "tabs")
    public Object[][] tabs() { return new Object[][] {{"All"},{"Unread"},{"Groups"}}; }

    @Test(groups = "authed", dataProvider = "tabs", description = "Clicking a chat tab activates it")
    public void tabActivates(String label) {
        assertTrue(gp.clickTabAndWaitActive("tabs", label),
                "tab did not become active: " + label);
    }

    @Test(groups = "authed", description = "Conversation filter input is present")
    public void filterPresent() { assertTrue(gp.shows(ChatPage.CONVO_FILTER)); }

    @Test(groups = "authed", description = "New chat button opens the new-chat dialog")
    public void newChatDialog() {
        gp.clickOn(ChatPage.NEW_CHAT);
        assertTrue(gp.howMany(ChatPage.NEW_CHAT_DIALOG) >= 1);
    }
}
