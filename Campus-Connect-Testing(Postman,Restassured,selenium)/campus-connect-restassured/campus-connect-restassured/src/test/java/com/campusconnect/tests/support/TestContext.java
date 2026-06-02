package com.campusconnect.tests.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared state across all test classes, mirroring the Postman "environment".
 *
 * Variables that Postman set via {@code pm.environment.set(...)} (tokens, ids, etc.)
 * are stored here and resolved into request URLs / bodies through {@link #render(String)}.
 *
 * A single instance is used for the whole suite so the request chain
 * (register -> login -> create post -> comment -> ...) flows exactly like the
 * Newman / Postman run.
 */
public final class TestContext {

    private static final TestContext INSTANCE = new TestContext();

    public static TestContext get() {
        return INSTANCE;
    }

    private final Map<String, String> vars = new ConcurrentHashMap<>();

    private TestContext() {
        init();
    }

    /**
     * Mirrors the collection-level pre-request script:
     * generates a runId once and derives the three unique e-mail addresses.
     * Also seeds baseUrl + password (from the Postman environment file).
     */
    private void init() {
        String baseUrl = System.getProperty("baseUrl",
                System.getenv().getOrDefault("BASE_URL", "http://localhost:8080"));
        set("baseUrl", baseUrl);
        set("password", "Passw0rd!");

        String runId = String.valueOf(System.currentTimeMillis());
        set("runId", runId);
        set("user1Email", "user1_" + runId + "@campus.test");
        set("user2Email", "user2_" + runId + "@campus.test");
        set("adminEmail", "admin_" + runId + "@campus.test");
    }

    public void set(String key, Object value) {
        if (value != null) {
            vars.put(key, String.valueOf(value));
        }
    }

    public String get(String key) {
        return vars.get(key);
    }

    public boolean has(String key) {
        String v = vars.get(key);
        return v != null && !v.isEmpty();
    }

    /**
     * Replaces every {@code {{var}}} token in the given text with its current value.
     * Unknown tokens are left untouched (so a missing id surfaces as a clear 4xx,
     * exactly as it would in Postman).
     */
    public String render(String text) {
        if (text == null) {
            return null;
        }
        String out = text;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            out = out.replace("{{" + e.getKey() + "}}", e.getValue());
        }
        return out;
    }
}
