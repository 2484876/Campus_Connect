package com.campusconnect.tests.support;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeSuite;

import java.util.List;

import static io.restassured.config.LogConfig.logConfig;

/**
 * Base class for every test class.
 *
 * Provides:
 *  - one-time Rest Assured configuration
 *  - the shared {@link TestContext}
 *  - request builders that attach the right bearer token (token1 / token2 / adminToken / none)
 *  - body / url rendering against the context
 *  - assertion + extraction helpers that mirror the Postman test scripts
 */
public abstract class BaseTest {

    protected final TestContext ctx = TestContext.get();

    @BeforeSuite(alwaysRun = true)
    public void configureRestAssured() {
        RestAssured.urlEncodingEnabled = false; // URLs already contain encoded query strings
        RestAssured.config = RestAssured.config()
                .logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL));
        // Log every request/response into the Extent report automatically.
        RestAssured.replaceFiltersWith(new RestAssuredReportFilter());
    }

    // ----------------------------------------------------------------------
    // Request builders
    // ----------------------------------------------------------------------

    /** A request with no Authorization header (Postman "noauth"). */
    protected RequestSpecification noAuth() {
        return RestAssured.given()
                .spec(new RequestSpecBuilder().build());
    }

    /** A request authenticated with the token stored under {@code tokenVar}. */
    protected RequestSpecification auth(String tokenVar) {
        RequestSpecification spec = RestAssured.given();
        String token = ctx.get(tokenVar);
        if (token != null && !token.isEmpty()) {
            spec.header("Authorization", "Bearer " + token);
        }
        return spec;
    }

    /** Default auth used by the collection (bearer {{token1}}). */
    protected RequestSpecification authDefault() {
        return auth("token1");
    }

    /** Attach a JSON body, rendering any {@code {{var}}} placeholders first. */
    protected RequestSpecification json(RequestSpecification spec, String rawBody) {
        return spec.contentType(ContentType.JSON).body(ctx.render(rawBody));
    }

    /** Resolve a URL template ({{baseUrl}}/api/...) against the context. */
    protected String url(String template) {
        return ctx.render(template);
    }

    // ----------------------------------------------------------------------
    // Assertions (mirror the pm.test status checks)
    // ----------------------------------------------------------------------

    /** Assert the response status code is one of the accepted codes. */
    protected void assertStatus(Response r, int... accepted) {
        int code = r.statusCode();
        for (int c : accepted) {
            if (code == c) {
                return;
            }
        }
        throw new AssertionError("Expected status in " + java.util.Arrays.toString(accepted)
                + " but got " + code + ". Body: " + safeBody(r));
    }

    /** Assert the JSON response body has a (non-null) property at the given path. */
    protected void assertHasProperty(Response r, String path) {
        Object value = tryPath(r, path);
        if (value == null) {
            throw new AssertionError("Expected response body to have property '" + path
                    + "'. Body: " + safeBody(r));
        }
    }

    // ----------------------------------------------------------------------
    // Extraction (mirror pm.environment.set(...))
    // ----------------------------------------------------------------------

    /** If {@code jsonPath} exists in the body, store its value under {@code var}. */
    protected void saveIfPresent(Response r, String jsonPath, String var) {
        Object value = tryPath(r, jsonPath);
        if (value != null) {
            ctx.set(var, value);
        }
    }

    // ----------------------------------------------------------------------
    // internals
    // ----------------------------------------------------------------------

    private Object tryPath(Response r, String path) {
        try {
            return r.jsonPath().get(path);
        } catch (Exception e) {
            return null;
        }
    }

    private String safeBody(Response r) {
        try {
            String b = r.asString();
            return b.length() > 600 ? b.substring(0, 600) + "..." : b;
        } catch (Exception e) {
            return "<unreadable>";
        }
    }

    protected static final List<Integer> NONE = List.of();
}
