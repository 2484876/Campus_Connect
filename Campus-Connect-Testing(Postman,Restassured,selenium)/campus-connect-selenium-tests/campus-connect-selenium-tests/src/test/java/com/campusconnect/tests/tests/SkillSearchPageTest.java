package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.SkillSearchPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** /skills UI. Group "authed". */
public class SkillSearchPageTest extends BaseAuthedTest {

    private SkillSearchPage page;

    @BeforeMethod(alwaysRun = true)
    public void open() { page = new SkillSearchPage(driver).openPage(); }

    @Test(groups = "authed", description = "Heading is 'Find by skill'")
    public void heading() { assertEquals(page.heading(), "Find by skill"); }

    @Test(groups = "authed", description = "Skill search input present")
    public void search() { assertTrue(page.hasSearch()); }

    @Test(groups = "authed", description = "Search button present")
    public void searchBtn() { assertTrue(page.hasSearchBtn()); }
}
