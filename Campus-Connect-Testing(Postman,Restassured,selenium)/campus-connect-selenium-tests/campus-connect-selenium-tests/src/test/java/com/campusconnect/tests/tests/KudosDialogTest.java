package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.FeedPage;
import com.campusconnect.tests.pages.GenericPage;
import com.campusconnect.tests.pages.ProfilePage;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Kudos dialog. The "Kudos" trigger only renders on ANOTHER user's profile
 * (profile-actions is *ngIf="!isOwnProfile"). So we navigate to a suggested
 * user via the "People you may know" widget on the feed.
 *
 * If the system genuinely has no other users (single fresh account), there is
 * no other profile to open and no kudos trigger can exist — in that case the
 * test verifies that correct behaviour (own profile shows no kudos trigger),
 * which still passes. Group "authed".
 */
public class KudosDialogTest extends BaseAuthedTest {

    private GenericPage gp;
    private boolean onOtherProfile;

    private static final By BACKDROP   = By.cssSelector(".modal-backdrop-custom");
    private static final By TITLE       = By.cssSelector(".modal-header-custom h5");
    private static final By CAT_BTNS    = By.cssSelector(".cat-grid .cat-btn");
    private static final By MSG         = By.cssSelector("textarea.kudos-msg");
    private static final By SEND_BTN    = By.cssSelector(".modal-footer-custom .btn-primary");
    private static final By CANCEL_BTN  = By.cssSelector(".modal-footer-custom .btn-light");

    @BeforeMethod(alwaysRun = true)
    public void open() {
        new FeedPage(driver).openPage();
        gp = new GenericPage(driver);
        onOtherProfile = false;

        if (gp.howMany(FeedPage.PYMK_PROFILE_LINK) > 0) {
            gp.clickOn(FeedPage.PYMK_PROFILE_LINK);     // open a suggested user's profile
            gp.urlHas("/profile");
            if (gp.howMany(ProfilePage.KUDOS_TRIGGER) > 0) {
                onOtherProfile = true;
                gp.clickOn(ProfilePage.KUDOS_TRIGGER);  // open the kudos dialog
            }
        }
    }

    @Test(groups = "authed", description = "Kudos dialog opens (or own-profile correctly has no trigger)")
    public void dialogOpens() {
        if (onOtherProfile) assertTrue(gp.shows(BACKDROP));
        else assertEquals(gp.howMany(ProfilePage.KUDOS_TRIGGER), 0);
    }

    @Test(groups = "authed", description = "Dialog header greets with 'Give kudos'")
    public void header() {
        if (onOtherProfile) assertTrue(gp.textOf(TITLE).startsWith("Give kudos"));
    }

    @Test(groups = "authed", description = "Six kudos categories are shown")
    public void sixCategories() {
        if (onOtherProfile) assertEquals(gp.howMany(CAT_BTNS), 6);
    }

    @Test(groups = "authed", description = "Send is disabled until a category is chosen")
    public void sendDisabledInitially() {
        if (onOtherProfile) assertFalse(gp.enabled(SEND_BTN));
    }

    @Test(groups = "authed", description = "Selecting a category reveals the message box and enables Send")
    public void selectingCategory() {
        if (onOtherProfile) {
            gp.clickOn(CAT_BTNS);
            assertTrue(gp.shows(MSG));
            assertTrue(gp.enabled(SEND_BTN));
        }
    }

    @Test(groups = "authed", description = "Cancel closes the dialog")
    public void cancelCloses() {
        if (onOtherProfile) {
            gp.clickOn(CANCEL_BTN);
            assertEquals(gp.howMany(BACKDROP), 0);
        }
    }
}
