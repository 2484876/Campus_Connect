package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.AchievementsPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** /achievements UI. Group "authed". */
public class AchievementsPageTest extends BaseAuthedTest {

    private AchievementsPage page;

    @BeforeMethod(alwaysRun = true)
    public void open() { page = new AchievementsPage(driver).openPage(); }

    @Test(groups = "authed", description = "Heading is 'Your achievements'")
    public void heading() { assertEquals(page.heading(), "Your achievements"); }

    @Test(groups = "authed", description = "Stat labels (Earned/Total/Points) present")
    public void stats() { assertTrue(page.statLabelCount() >= 3); }
}
