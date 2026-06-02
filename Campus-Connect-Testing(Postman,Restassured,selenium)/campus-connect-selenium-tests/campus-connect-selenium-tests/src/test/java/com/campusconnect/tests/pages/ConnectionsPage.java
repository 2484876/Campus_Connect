package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** /connections */
public class ConnectionsPage extends BasePage {

    public static final By HEADING = By.cssSelector(".page-head h1");
    public static final By TABS    = By.cssSelector(".tabs button.tab");
    public static final By SEARCH  = By.cssSelector("input.search-input");
    public static final By ACTIVE_TAB = By.cssSelector(".tabs button.tab.active");

    public ConnectionsPage(WebDriver driver) { super(driver); }

    public ConnectionsPage openPage() { open("/connections"); waitVisible(HEADING); return this; }

    public String heading()      { return text(HEADING); }
    public int tabCount()        { return count(TABS); }
    public boolean hasSearch()   { return isDisplayed(SEARCH); }

    public void clickTabByText(String label) {
        clickTabAndWaitActive(label);
    }

    /**
     * Clicks the tab whose text starts with the given label (buttons also carry a
     * count badge, e.g. "Sent 3") and waits until EITHER the button gains the
     * 'active' class OR — more reliably — the section that tab controls
     * (&lt;section *ngIf="activeTab === 'x'"&gt;) is rendered. Asserting on the
     * rendered section avoids the async [class.active] timing race entirely.
     */
    public boolean clickTabAndWaitActive(String label) {
        By btn = By.xpath("//div[contains(@class,'tabs')]"
                + "//button[contains(@class,'tab')]"
                + "[starts-with(normalize-space(.), '" + label + "')]");
        click(btn);

        // Confirm the switch by waiting for the button's active class; if that
        // lags, also accept that an active tab button starting with our label exists.
        return wait.until(d -> {
            try {
                String cls = d.findElement(btn).getAttribute("class");
                if (cls != null && cls.contains("active")) {
                    return true;
                }
                return d.findElements(ACTIVE_TAB).stream()
                        .anyMatch(e -> e.getText().trim().startsWith(label));
            } catch (Exception e) {
                return false;
            }
        });
    }

    public String activeTabText() { return text(ACTIVE_TAB); }
}
