package com.campusconnect.tests.base;

import com.campusconnect.tests.config.Config;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Builds a WebDriver. Selenium 4's built-in Selenium Manager downloads the
 * matching driver binary automatically the first time, so there is no manual
 * ChromeDriver setup and no WebDriverManager dependency required.
 */
public final class DriverFactory {

    static {
        // Silence the noisy "Unable to find CDP implementation matching 1xx" warnings.
        // They are harmless (Selenium Manager still drives Chrome fine) but flood the log.
        java.util.logging.Logger.getLogger("org.openqa.selenium")
                .setLevel(java.util.logging.Level.SEVERE);
    }

    private DriverFactory() { }

    public static WebDriver create() {
        boolean headless = Config.headless();
        switch (Config.browser()) {
            case "firefox": {
                FirefoxOptions o = new FirefoxOptions();
                if (headless) o.addArguments("-headless");
                return new FirefoxDriver(o);
            }
            case "edge": {
                EdgeOptions o = new EdgeOptions();
                if (headless) o.addArguments("--headless=new");
                o.addArguments("--window-size=1440,900");
                return new EdgeDriver(o);
            }
            case "chrome":
            default: {
                ChromeOptions o = new ChromeOptions();
                if (headless) o.addArguments("--headless=new");
                o.addArguments("--window-size=1440,900");
                o.addArguments("--remote-allow-origins=*");
                o.addArguments("--disable-gpu");
                o.addArguments("--no-sandbox");
                return new ChromeDriver(o);
            }
        }
    }
}
