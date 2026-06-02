package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** /notifications */
public class NotificationsPage extends BasePage {

    public static final By HEADING = By.cssSelector(".notif-page-header h5");

    public NotificationsPage(WebDriver driver) { super(driver); }

    public NotificationsPage openPage() { open("/notifications"); waitVisible(HEADING); return this; }

    public String heading() { return text(HEADING); }
}
