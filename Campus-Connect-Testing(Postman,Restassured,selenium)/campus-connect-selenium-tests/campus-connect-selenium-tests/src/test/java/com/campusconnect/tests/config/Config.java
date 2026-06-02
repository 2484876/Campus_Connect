package com.campusconnect.tests.config;

import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * Central configuration. Resolution order for every key:
 *   1. JVM system property (-Dkey=value on the mvn command line)
 *   2. value in src/test/resources/config.properties
 *   3. hard-coded fallback
 */
public final class Config {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Config.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load config.properties", e);
        }
    }

    private Config() { }

    private static String get(String key, String fallback) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank() && !sys.startsWith("${")) {
            return sys.trim();
        }
        return PROPS.getProperty(key, fallback).trim();
    }

    /** Base URL of the running Angular app, e.g. http://localhost:4200 (no trailing slash). */
    public static String baseUrl() {
        String url = get("base.url", "http://localhost:4200");
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public static String browser() {
        return get("browser", "chrome").toLowerCase();
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless", "false"));
    }

    public static Duration explicitWait() {
        return Duration.ofSeconds(Long.parseLong(get("explicit.wait.seconds", "15")));
    }
}
