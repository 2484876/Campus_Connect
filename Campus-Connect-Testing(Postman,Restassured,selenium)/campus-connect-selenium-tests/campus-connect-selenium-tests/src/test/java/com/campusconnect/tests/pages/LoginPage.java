package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for the /login route.
 *
 * Locators are derived directly from login.html. NOTE: the inputs have no id or
 * name attributes, so we target them by type (email/password are each unique on
 * this page) and the submit button by its CSS class.
 */
public class LoginPage extends BasePage {

    // ---- Locators ----
    public static final By HEADING      = By.cssSelector(".auth-card h2");          // "Sign in"
    public static final By EMAIL        = By.cssSelector("input[type='email']");
    public static final By PASSWORD     = By.cssSelector("input[type='password']");
    public static final By SUBMIT_BTN   = By.cssSelector("button.btn-primary");
    public static final By ERROR_ALERT  = By.cssSelector(".alert.alert-danger");
    public static final By JOIN_LINK     = By.linkText("Join now");
    public static final By LOGO          = By.cssSelector(".auth-header img");
    public static final By SUBTITLE      = By.cssSelector(".auth-header p");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage openPage() {
        open("/login");
        waitVisible(SUBMIT_BTN);
        return this;
    }

    public LoginPage enterEmail(String email) {
        type(EMAIL, email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(PASSWORD, password);
        return this;
    }

    public LoginPage clickSignIn() {
        click(SUBMIT_BTN);
        return this;
    }

    /** Fill both fields and submit. */
    public LoginPage login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
        return this;
    }

    public RegisterPage goToRegister() {
        click(JOIN_LINK);
        return new RegisterPage(driver).waitLoaded();
    }

    // ---- Queries / assertions support ----
    public LoginPage waitLoaded() {
        waitVisible(SUBMIT_BTN);
        return this;
    }

    public String heading()          { return text(HEADING); }
    public boolean hasEmailField()    { return isDisplayed(EMAIL); }
    public boolean hasPasswordField() { return isDisplayed(PASSWORD); }
    public boolean submitEnabled()    { return isEnabled(SUBMIT_BTN); }
    public String submitText()        { return text(SUBMIT_BTN); }
    public String emailPlaceholder()  { return attr(EMAIL, "placeholder"); }
    public String passwordType()      { return attr(PASSWORD, "type"); }
    public boolean hasJoinLink()      { return isDisplayed(JOIN_LINK); }

    public boolean isErrorDisplayed() {
        return isDisplayed(ERROR_ALERT);
    }

    public String errorText() {
        return wait.until(ExpectedConditions
                .visibilityOfElementLocated(ERROR_ALERT)).getText().trim();
    }
}
