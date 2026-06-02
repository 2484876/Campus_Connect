package com.campusconnect.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

/**
 * Page object for the /register route.
 *
 * Inputs have no id/name. Email and password are unique by type; the three text
 * inputs (name, department, position) are disambiguated by their placeholder.
 */
public class RegisterPage extends BasePage {

    // ---- Locators ----
    public static final By HEADING     = By.cssSelector(".auth-header h1");          // "Join Campus Connect"
    public static final By NAME        = By.cssSelector("input[placeholder='Rahul Kumar']");
    public static final By EMAIL       = By.cssSelector("input[type='email']");
    public static final By PASSWORD    = By.cssSelector("input[type='password']");
    public static final By ROLE_SELECT = By.cssSelector("select.form-control");
    public static final By DEPARTMENT  = By.cssSelector("input[placeholder='Digital Engineering']");
    public static final By POSITION    = By.cssSelector("input[placeholder='Full Stack Developer']");
    public static final By SUBMIT_BTN  = By.cssSelector("button.btn-primary");
    public static final By ERROR_ALERT = By.cssSelector(".alert.alert-danger");
    public static final By SIGNIN_LINK = By.linkText("Sign in");
    public static final By ROLE_OPTIONS = By.cssSelector("select.form-control option");
    public static final By LOGO        = By.cssSelector(".auth-header img");
    public static final By SUBTITLE    = By.cssSelector(".auth-header p");
    public static final By FORM_GROUPS = By.cssSelector(".form-group");
    public static By roleOption(String value) { return By.cssSelector("select.form-control option[value='" + value + "']"); }

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public RegisterPage openPage() {
        open("/register");
        waitVisible(SUBMIT_BTN);
        return this;
    }

    public RegisterPage waitLoaded() {
        waitVisible(SUBMIT_BTN);
        return this;
    }

    public RegisterPage enterName(String name)          { type(NAME, name); return this; }
    public RegisterPage enterEmail(String email)        { type(EMAIL, email); return this; }
    public RegisterPage enterPassword(String password)  { type(PASSWORD, password); return this; }
    public RegisterPage enterDepartment(String dept)    { type(DEPARTMENT, dept); return this; }
    public RegisterPage enterPosition(String position)  { type(POSITION, position); return this; }

    public RegisterPage selectRoleByValue(String value) {
        new Select(waitVisible(ROLE_SELECT)).selectByValue(value);
        return this;
    }

    public RegisterPage clickJoin() {
        click(SUBMIT_BTN);
        return this;
    }

    /** Minimal valid registration (only the three required fields). */
    public RegisterPage register(String name, String email, String password) {
        enterName(name);
        enterEmail(email);
        enterPassword(password);
        clickJoin();
        return this;
    }

    public LoginPage goToLogin() {
        click(SIGNIN_LINK);
        return new LoginPage(driver).waitLoaded();
    }

    // ---- Queries ----
    public String heading()           { return text(HEADING); }
    public boolean hasNameField()     { return isDisplayed(NAME); }
    public boolean hasEmailField()    { return isDisplayed(EMAIL); }
    public boolean hasPasswordField() { return isDisplayed(PASSWORD); }
    public boolean hasRoleSelect()    { return isDisplayed(ROLE_SELECT); }
    public boolean submitEnabled()    { return isEnabled(SUBMIT_BTN); }
    public boolean hasSignInLink()    { return isDisplayed(SIGNIN_LINK); }
    public int roleOptionCount()      { return count(ROLE_OPTIONS); }

    public String selectedRoleValue() {
        WebElement sel = waitVisible(ROLE_SELECT);
        return new Select(sel).getFirstSelectedOption().getAttribute("value");
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(ERROR_ALERT);
    }

    public String errorText() {
        return wait.until(ExpectedConditions
                .visibilityOfElementLocated(ERROR_ALERT)).getText().trim();
    }
}
