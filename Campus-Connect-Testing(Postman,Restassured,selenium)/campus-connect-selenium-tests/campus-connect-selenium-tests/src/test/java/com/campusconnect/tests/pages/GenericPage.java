package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

/**
 * Thin public wrapper over BasePage helpers so the *Extended test classes can
 * assert against the public locator constants already declared on each page
 * object, without duplicating a method per check.
 */
public class GenericPage extends BasePage {

    public GenericPage(WebDriver driver) { super(driver); }

    public GenericPage go(String path)         { open(path); return this; }
    public boolean shows(By by)                { return isDisplayed(by); }
    public int howMany(By by)                  { return count(by); }
    public String textOf(By by)                { return text(by); }
    public String attrOf(By by, String name)   { return attr(by, name); }
    public boolean enabled(By by)              { return isEnabled(by); }
    public void clickOn(By by)                 { click(by); }
    public void typeInto(By by, String v)      { type(by, v); }
    public String valueOf(By by)               { return attr(by, "value"); }
    public boolean urlHas(String frag)         { return waitUrlContains(frag); }

    public void pressEnter(By by) {
        waitVisible(by).sendKeys(Keys.ENTER);
    }

    /**
     * Clicks a tab button (located by its exact visible text within a tabs
     * container) and waits until THAT button gains the 'active' class. Returns
     * true once active. Robust against Angular's async [class.active] binding.
     *
     * @param tabsContainerClass e.g. "tabs" or "tabs-bar"
     * @param label              exact button text, e.g. "Unread"
     */
    public boolean clickTabAndWaitActive(String tabsContainerClass, String label) {
        By btn = By.xpath("//div[contains(@class,'" + tabsContainerClass + "')]"
                + "//button[normalize-space(.)='" + label + "']");
        click(btn);
        return wait.until(d -> {
            try {
                String cls = d.findElement(btn).getAttribute("class");
                return cls != null && cls.contains("active");
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Like clickTabAndWaitActive but matches a button whose text STARTS WITH the
     * label (tolerates trailing count badges) within the given container.
     */
    public boolean clickTabStartsWithAndWaitActive(String tabsContainerClass, String label) {
        By btn = By.xpath("//div[contains(@class,'" + tabsContainerClass + "')]"
                + "//button[starts-with(normalize-space(.), '" + label + "')]");
        click(btn);
        return wait.until(d -> {
            try {
                String cls = d.findElement(btn).getAttribute("class");
                return cls != null && cls.contains("active");
            } catch (Exception e) {
                return false;
            }
        });
    }
}
