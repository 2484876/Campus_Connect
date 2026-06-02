package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.AchievementsPage;
import com.campusconnect.tests.pages.GenericPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Achievements stats + grid. Group "authed". */
public class AchievementsExtendedTest extends BaseAuthedTest {

    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() { new AchievementsPage(driver).openPage(); gp = new GenericPage(driver); }

    @Test(groups = "authed", description = "Three stat figures are shown (Earned/Total/Points)")
    public void statNumbers() { assertTrue(gp.howMany(AchievementsPage.STAT_NUMS) >= 3); }

    @Test(groups = "authed", description = "Achievements grid is rendered")
    public void gridShown() { assertTrue(gp.shows(AchievementsPage.GRID)); }
}
