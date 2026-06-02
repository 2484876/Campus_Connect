package com.campusconnect.tests.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Applies RetryAnalyzer to every @Test automatically, so individual tests are
 * re-run on failure without having to add retryAnalyzer = ... to each one.
 *
 * Registered via <listeners> in the testng suites.
 */
public class RetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
