package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.NotificationsPage;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** /notifications UI. Group "authed". */
public class NotificationsPageTest extends BaseAuthedTest {

    @Test(groups = "authed", description = "Heading is 'Notifications'")
    public void heading() {
        NotificationsPage page = new NotificationsPage(driver).openPage();
        assertEquals(page.heading(), "Notifications");
    }
}
