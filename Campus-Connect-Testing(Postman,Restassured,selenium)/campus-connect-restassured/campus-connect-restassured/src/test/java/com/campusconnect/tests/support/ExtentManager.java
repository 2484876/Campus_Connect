package com.campusconnect.tests.support;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Creates and configures the ExtentReports "Spark" HTML report.
 * Produces a polished, single-file dashboard at:
 *     target/extent-report/CampusConnect-API-Report.html
 */
public final class ExtentManager {

    private static ExtentReports extent;
    private static String reportPath;

    private ExtentManager() { }

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            extent = create();
        }
        return extent;
    }

    public static String getReportPath() {
        return reportPath;
    }

    private static ExtentReports create() {
        String dir = "target/extent-report";
        new File(dir).mkdirs();
        reportPath = dir + "/CampusConnect-API-Report.html";

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("Campus Connect — API Test Report");
        spark.config().setReportName("Campus Connect · Rest Assured Suite");
        spark.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a");
        spark.config().setEncoding("utf-8");
        // A little extra polish: tighter cards, monospace logs, colored status pills.
        spark.config().setCss(
                ".test-content .card .name{font-weight:600;}" +
                ".badge{border-radius:10px;}" +
                "pre,code{font-family:'JetBrains Mono',Consolas,monospace;font-size:12px;}" +
                ".logs td{vertical-align:top;}");

        ExtentReports e = new ExtentReports();
        e.attachReporter(spark);
        e.setSystemInfo("Project", "Campus Connect");
        e.setSystemInfo("Suite", "Rest Assured · 188 requests · 22 modules");
        e.setSystemInfo("Base URL", TestContext.get().get("baseUrl"));
        e.setSystemInfo("Run started", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        e.setSystemInfo("Java", System.getProperty("java.version"));
        e.setSystemInfo("OS", System.getProperty("os.name"));
        return e;
    }
}
