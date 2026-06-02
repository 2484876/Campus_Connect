package com.campusconnect.tests.support;

import com.aventstack.extentreports.ExtentTest;

/**
 * Holds the {@link ExtentTest} node for the currently executing test so that the
 * Rest Assured filter can attach request/response logs to the right entry.
 * The suite runs single-threaded, but a ThreadLocal keeps it safe either way.
 */
public final class ExtentTestManager {

    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();

    private ExtentTestManager() { }

    public static void set(ExtentTest test) {
        CURRENT.set(test);
    }

    public static ExtentTest get() {
        return CURRENT.get();
    }

    public static void remove() {
        CURRENT.remove();
    }
}
