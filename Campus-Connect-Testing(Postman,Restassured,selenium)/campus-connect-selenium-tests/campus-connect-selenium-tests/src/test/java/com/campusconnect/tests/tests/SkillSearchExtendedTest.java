package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.GenericPage;
import com.campusconnect.tests.pages.SkillSearchPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Skill-search deeper checks. Group "authed". */
public class SkillSearchExtendedTest extends BaseAuthedTest {

    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() { new SkillSearchPage(driver).openPage(); gp = new GenericPage(driver); }

    @Test(groups = "authed", description = "Search input accepts text")
    public void typing() {
        gp.typeInto(SkillSearchPage.SEARCH, "Java");
        assertEquals(gp.valueOf(SkillSearchPage.SEARCH), "Java");
    }

    @Test(groups = "authed", description = "Search button is enabled")
    public void buttonEnabled() { assertTrue(gp.enabled(SkillSearchPage.SEARCH_BTN)); }

    @Test(groups = "authed", description = "Search input has a helpful placeholder")
    public void placeholder() {
        assertTrue(gp.attrOf(SkillSearchPage.SEARCH, "placeholder").toLowerCase().contains("skill"));
    }
}
