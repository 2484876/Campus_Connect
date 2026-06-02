package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.BookmarksPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** /bookmarks UI. Group "authed". */
public class BookmarksPageTest extends BaseAuthedTest {

    private BookmarksPage page;

    @BeforeMethod(alwaysRun = true)
    public void open() { page = new BookmarksPage(driver).openPage(); }

    @Test(groups = "authed", description = "Heading is 'Saved posts'")
    public void heading() { assertEquals(page.heading(), "Saved posts"); }

    @Test(groups = "authed", description = "Subtitle present")
    public void subtitle() { assertTrue(page.hasSubtitle()); }
}
