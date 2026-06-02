package com.campusconnect.tests.pages;

import com.campusconnect.tests.config.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/** Common interaction helpers shared by every page object. */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Config.explicitWait());
    }

    protected void open(String path) {
        driver.get(Config.baseUrl() + path);
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void type(By locator, String text) {
        WebElement el = waitVisible(locator);
        el.clear();
        if (text != null && !text.isEmpty()) {
            el.sendKeys(text);
        }
    }

    protected void click(By locator) {
        WebElement el = waitClickable(locator);
        try {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        } catch (Exception ignored) { /* scrolling is best-effort */ }
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            // Something (e.g. the toast overlay) covered the element — click via JS.
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    protected String text(By locator) {
        return waitVisible(locator).getText().trim();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected boolean isEnabled(By locator) {
        return waitVisible(locator).isEnabled();
    }

    protected String attr(By locator, String name) {
        return waitVisible(locator).getAttribute(name);
    }

    protected int count(By locator) {
        List<WebElement> els = driver.findElements(locator);
        return els.size();
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    /** Waits until the URL path contains the given fragment (e.g. "/feed"). */
    public boolean waitUrlContains(String fragment) {
        return wait.until(ExpectedConditions.urlContains(fragment));
    }
}
