package com.campusconnect.tests.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Re-runs a failing test up to MAX_RETRIES extra times. If any attempt passes,
 * TestNG records the test as passed. If every attempt fails, it stays failed.
 *
 * This absorbs genuine flakiness (timing/animation/render races). It does NOT
 * and should NOT mask a consistently failing test — a real bug fails all
 * attempts and remains red.
 *
 * Override the retry count from the command line:
 *   mvn test -Dretry.count=3
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRIES =
            Integer.parseInt(System.getProperty("retry.count", "2"));

    private int attempt = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (attempt < MAX_RETRIES) {
            attempt++;
            return true;   // run it again
        }
        return false;      // give up; record the failure
    }

    /** 1-based attempt number for logging (1 = first run). */
    public int currentAttempt() {
        return attempt + 1;
    }
}
