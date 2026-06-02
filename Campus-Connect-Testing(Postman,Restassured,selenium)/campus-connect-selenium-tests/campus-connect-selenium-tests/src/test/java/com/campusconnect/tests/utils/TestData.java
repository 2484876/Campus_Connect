package com.campusconnect.tests.utils;

/** Helpers for generating test data. */
public final class TestData {

    private TestData() { }

    /** A unique, valid-looking email so repeated E2E runs never collide. */
    public static String uniqueEmail() {
        return "qa.user." + System.currentTimeMillis() + "@campus.edu";
    }

    public static String defaultPassword() {
        return "Test@1234";   // satisfies the "min 6 characters" rule
    }

    public static String defaultName() {
        return "QA Automation User";
    }
}
