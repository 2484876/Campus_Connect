package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** /achievements */
public class AchievementsPage extends BasePage {

    public static final By HEADING = By.cssSelector(".ach-header h2");
    public static final By STAT_LABELS = By.cssSelector(".stat-label");
    public static final By STAT_NUMS = By.cssSelector(".stat-num");
    public static final By GRID = By.cssSelector(".ach-grid");

    public AchievementsPage(WebDriver driver) { super(driver); }

    public AchievementsPage openPage() { open("/achievements"); waitVisible(HEADING); return this; }

    public String heading()     { return text(HEADING); }
    public int statLabelCount() { return count(STAT_LABELS); }
}
