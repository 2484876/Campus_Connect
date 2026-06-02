package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.FeedPage;
import com.campusconnect.tests.pages.GenericPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Post interactions on the feed: like/unlike toggle and the comment box.
 *
 * This test SELF-SEEDS: it creates a fresh post in @BeforeMethod so there is
 * always something to like/comment, regardless of account. No skips.
 * Group "authed".
 */
public class FeedLikeCommentTest extends BaseAuthedTest {

    private FeedPage feed;
    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() {
        feed = new FeedPage(driver).openPage();
        feed.createPost("Automation seed post " + System.currentTimeMillis());
        gp = new GenericPage(driver);
        // the freshly created post appears at the top; its Like button is the first one
        gp.urlHas("/feed");
    }

    @Test(groups = "authed", description = "Like button toggles label between Like and Liked")
    public void likeToggles() {
        String before = gp.textOf(FeedPage.LIKE_LABEL);
        gp.clickOn(FeedPage.LIKE_BTN);
        boolean flipped = new org.openqa.selenium.support.ui.WebDriverWait(
                driver, java.time.Duration.ofSeconds(10))
                .until(d -> !d.findElement(FeedPage.LIKE_LABEL).getText().trim().equals(before));
        assertTrue(flipped, "Like label did not change after click");
        String after = gp.textOf(FeedPage.LIKE_LABEL);
        assertNotEquals(after, before);
        assertTrue(after.equals("Like") || after.equals("Liked"));
    }

    @Test(groups = "authed", description = "Like then unlike returns to the original label")
    public void likeThenUnlike() {
        String before = gp.textOf(FeedPage.LIKE_LABEL);
        gp.clickOn(FeedPage.LIKE_BTN);
        gp.clickOn(FeedPage.LIKE_BTN);
        boolean back = new org.openqa.selenium.support.ui.WebDriverWait(
                driver, java.time.Duration.ofSeconds(10))
                .until(d -> d.findElement(FeedPage.LIKE_LABEL).getText().trim().equals(before));
        assertTrue(back, "Label did not return to original after unlike");
    }

    @Test(groups = "authed", description = "Comment button reveals the add-comment input")
    public void commentBoxOpens() {
        gp.clickOn(FeedPage.COMMENT_BTN);
        assertTrue(gp.shows(FeedPage.COMMENT_INPUT));
    }

    @Test(groups = "authed", description = "Typing a comment reflects in the input")
    public void commentTyping() {
        gp.clickOn(FeedPage.COMMENT_BTN);
        gp.typeInto(FeedPage.COMMENT_INPUT, "Great post!");
        assertEquals(gp.valueOf(FeedPage.COMMENT_INPUT), "Great post!");
    }
}
