package com.campusconnect.tests.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.campusconnect.tests.base.BaseTest;
import com.campusconnect.tests.config.Config;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Generates a polished standalone HTML report (ExtentReports "Spark") at
 *   target/extent-report/index.html
 * Logs every test start / pass / fail / skip, attaches the failure stack trace,
 * and embeds a screenshot (base64) on failure.
 *
 * Registered via <listeners> in the testng xml suites.
 */
public class ExtentReportListener implements ITestListener, ISuiteListener {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();
    // Reuse one report node across retry attempts of the same test+params.
    private static final java.util.Map<String, ExtentTest> NODES =
            new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public void onStart(ISuite suite) {
        if (extent != null) {
            return;
        }
        ExtentSparkReporter spark = new ExtentSparkReporter("target/extent-report/index.html");
        spark.config().setReportName("Campus Connect — UI Test Report");
        spark.config().setDocumentTitle("Campus Connect Selenium Report");
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Run at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        extent.setSystemInfo("Base URL", Config.baseUrl());
        extent.setSystemInfo("Browser", Config.browser());
        extent.setSystemInfo("Headless", String.valueOf(Config.headless()));
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java", System.getProperty("java.version"));
    }

    @Override
    public void onFinish(ISuite suite) {
        if (extent != null) {
            extent.flush();
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        String key = result.getTestClass().getRealClass().getName()
                + "#" + result.getMethod().getMethodName()
                + "#" + java.util.Arrays.toString(result.getParameters());

        // If this is a retry of a test we've already seen, reuse its node.
        ExtentTest existing = NODES.get(key);
        if (existing != null) {
            CURRENT.set(existing);
            existing.log(Status.INFO, "Retrying — attempt " + attemptNumber(result));
            return;
        }

        String name = result.getTestClass().getRealClass().getSimpleName()
                + " :: " + result.getMethod().getMethodName();
        ExtentTest test = extent.createTest(name);
        String desc = result.getMethod().getDescription();
        if (desc != null && !desc.isBlank()) {
            test.info(desc);
        }
        for (String g : result.getMethod().getGroups()) {
            test.assignCategory(g);
        }
        NODES.put(key, test);
        CURRENT.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = CURRENT.get();
        int attempt = attemptNumber(result);
        if (attempt > 1) {
            test.log(Status.PASS, "Passed on attempt " + attempt + " (recovered after retry)");
        } else {
            test.log(Status.PASS, "Passed");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = CURRENT.get();

        if (willRetry(result)) {
            // This attempt failed but TestNG will run it again — log as a soft
            // warning, not a hard failure, so the final report stays clean.
            test.log(Status.WARNING, "Attempt " + attemptNumber(result)
                    + " failed, retrying: " + shortMsg(result));
            return;
        }

        // Final attempt failed — this is a real failure.
        test.log(Status.FAIL, "Failed (all attempts exhausted): " + result.getThrowable());

        WebDriver driver = driverFrom(result);
        if (driver != null) {
            try {
                String b64 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
                test.addScreenCaptureFromBase64String(b64, "Failure screenshot");
            } catch (Exception ignored) { /* screenshot is best-effort */ }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = CURRENT.get();
        if (test == null) {
            test = extent.createTest(result.getMethod().getMethodName());
        }
        test.log(Status.SKIP, "Skipped: "
                + (result.getThrowable() != null ? result.getThrowable().getMessage() : ""));
    }

    /** True if a RetryAnalyzer is attached and still has retries left for this result. */
    private boolean willRetry(ITestResult result) {
        org.testng.IRetryAnalyzer ra = result.getMethod().getRetryAnalyzer(result);
        // RetryAnalyzer.retry() returns true if it WILL retry. We must not call it
        // here (it has side effects / increments), so infer from attempt count.
        if (ra instanceof RetryAnalyzer) {
            int max = Integer.parseInt(System.getProperty("retry.count", "2"));
            return attemptNumber(result) <= max;   // more attempts remain
        }
        return false;
    }

    private int attemptNumber(ITestResult result) {
        org.testng.IRetryAnalyzer ra = result.getMethod().getRetryAnalyzer(result);
        if (ra instanceof RetryAnalyzer) {
            return ((RetryAnalyzer) ra).currentAttempt();
        }
        return 1;
    }

    private String shortMsg(ITestResult result) {
        Throwable t = result.getThrowable();
        if (t == null) return "";
        String m = t.getMessage();
        return m == null ? t.getClass().getSimpleName() : m.split("\n")[0];
    }

    private WebDriver driverFrom(ITestResult result) {
        Object inst = result.getInstance();
        return (inst instanceof BaseTest) ? ((BaseTest) inst).getDriver() : null;
    }
}
