package com.campusconnect.tests.support;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Bridges TestNG test events into the ExtentReports dashboard.
 *
 * - Each test class becomes a logical group via the module label.
 * - Each @Test method becomes a report entry named after its Postman request.
 * - Pass / fail / skip are colour-coded; failures include the assertion message.
 *
 * Registered in testng.xml under &lt;listeners&gt;.
 */
public class ExtentReportListener implements ITestListener {

    private ExtentReports extent;

    @Override
    public void onStart(ITestContext context) {
        extent = ExtentManager.getInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String module = prettyModule(result.getTestClass().getRealClass().getSimpleName());
        String desc = result.getMethod().getDescription();
        String name = (desc != null && !desc.isEmpty()) ? desc : result.getName();

        ExtentTest test = extent.createTest(name)
                .assignCategory(module);
        ExtentTestManager.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest t = ExtentTestManager.get();
        if (t != null) {
            t.log(Status.PASS, MarkupHelper.createLabel("PASSED", ExtentColor.GREEN));
        }
        ExtentTestManager.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest t = ExtentTestManager.get();
        if (t != null) {
            t.log(Status.FAIL, MarkupHelper.createLabel("FAILED", ExtentColor.RED));
            Throwable thr = result.getThrowable();
            if (thr != null) {
                t.fail(thr.getMessage() == null ? thr.toString() : thr.getMessage());
            }
        }
        ExtentTestManager.remove();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest t = ExtentTestManager.get();
        if (t == null) {
            String desc = result.getMethod().getDescription();
            t = extent.createTest((desc != null && !desc.isEmpty()) ? desc : result.getName())
                    .assignCategory(prettyModule(result.getTestClass().getRealClass().getSimpleName()));
        }
        t.log(Status.SKIP, MarkupHelper.createLabel("SKIPPED", ExtentColor.AMBER));
        Throwable thr = result.getThrowable();
        if (thr != null) {
            t.skip(thr.getMessage() == null ? thr.toString() : thr.getMessage());
        }
        ExtentTestManager.remove();
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
            System.out.println("\n==================================================");
            System.out.println("  Extent report generated:");
            System.out.println("  " + ExtentManager.getReportPath());
            System.out.println("==================================================\n");
        }
    }

    /** "T11CommunitiesTest" -> "11 · Communities". */
    private String prettyModule(String className) {
        String s = className.replaceFirst("^T", "").replaceFirst("Test$", "");
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(\\d+)(.*)$").matcher(s);
        if (m.matches()) {
            String num = m.group(1);
            String rest = m.group(2).replaceAll("([a-z])([A-Z])", "$1 $2");
            return num + " · " + rest.trim();
        }
        return s;
    }
}
