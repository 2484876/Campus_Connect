package com.campusconnect.tests.support;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * Rest Assured filter that records every HTTP request/response into the current
 * ExtentTest node: method + URI, status code, response time, and (on non-2xx) a
 * trimmed response body. Wired in globally from {@link BaseTest}, so all 188
 * requests are logged automatically — no per-test plumbing needed.
 */
public class RestAssuredReportFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification req,
                           FilterableResponseSpecification res,
                           FilterContext fctx) {
        Response response = fctx.next(req, res);

        ExtentTest test = ExtentTestManager.get();
        if (test != null) {
            int code = response.statusCode();
            long ms = response.time();
            String method = req.getMethod();
            String uri = req.getURI();

            String pill = (code >= 200 && code < 300) ? "&#9679;" : "&#9888;";
            test.info(String.format("%s <b>%s</b> %s &rarr; <b>%d</b> <span>(%d ms)</span>",
                    pill, method, uri, code, ms));

            String body = safe(response);
            if (code < 200 || code >= 300) {
                if (!body.isEmpty()) {
                    test.info(MarkupHelper.createCodeBlock(body, CodeLanguage.JSON));
                }
            }
        }
        return response;
    }

    private String safe(Response r) {
        try {
            String b = r.asString();
            if (b == null) {
                return "";
            }
            return b.length() > 1200 ? b.substring(0, 1200) + " …(truncated)" : b;
        } catch (Exception e) {
            return "";
        }
    }
}
