package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseAuthedTest;
import com.campusconnect.tests.pages.HashtagPage;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** /hashtag/{tag} UI. Group "authed". */
public class HashtagPageTest extends BaseAuthedTest {

    @Test(groups = "authed", description = "Hashtag header reflects the tag in the URL")
    public void tagHeader() {
        HashtagPage page = new HashtagPage(driver).openTag("java");
        assertTrue(page.tagText().contains("java"),
                "Tag header should contain 'java' but was: " + page.tagText());
    }
}
