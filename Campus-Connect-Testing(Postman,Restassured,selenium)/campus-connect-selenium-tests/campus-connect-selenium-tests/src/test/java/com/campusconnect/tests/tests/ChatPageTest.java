package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.ChatPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** /chat UI. Group "authed". */
public class ChatPageTest extends BaseAuthedTest {

    private ChatPage page;

    @BeforeMethod(alwaysRun = true)
    public void open() { page = new ChatPage(driver).openPage(); }

    @Test(groups = "authed", description = "Heading is 'Messages'")
    public void heading() { assertEquals(page.heading(), "Messages"); }

    @Test(groups = "authed", description = "Three conversation tabs (All/Unread/Groups)")
    public void tabs() { assertEquals(page.tabCount(), 3); }

    @Test(groups = "authed", description = "Message search input present")
    public void search() { assertTrue(page.hasSearch()); }

    @Test(groups = "authed", description = "New chat button present")
    public void newChat() { assertTrue(page.hasNewChatBtn()); }

    @Test(groups = "authed", description = "Placeholder shown when no conversation selected")
    public void noChatPlaceholder() { assertTrue(page.hasNoChatPlaceholder()); }
}
