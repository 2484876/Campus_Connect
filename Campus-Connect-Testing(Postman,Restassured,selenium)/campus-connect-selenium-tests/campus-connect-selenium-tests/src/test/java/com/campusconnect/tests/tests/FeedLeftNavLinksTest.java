package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.FeedPage;
import com.campusconnect.tests.pages.GenericPage;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/** The five quick links in the feed's left profile card. Group "authed". */
public class FeedLeftNavLinksTest extends BaseAuthedTest {

    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() { new FeedPage(driver).openPage(); gp = new GenericPage(driver); }

    @DataProvider(name = "links")
    public Object[][] links() {
        return new Object[][] {{"View profile"},{"Saved posts"},{"Achievements"},{"My network"},{"Find by skill"}};
    }

    @Test(groups = "authed", dataProvider = "links", description = "Left-card quick link is present")
    public void linkPresent(String label) {
        assertTrue(gp.shows(By.xpath(
            "//div[contains(@class,'profile-links')]//a[contains(normalize-space(.),'" + label + "')]")),
            "missing left link: " + label);
    }
}
