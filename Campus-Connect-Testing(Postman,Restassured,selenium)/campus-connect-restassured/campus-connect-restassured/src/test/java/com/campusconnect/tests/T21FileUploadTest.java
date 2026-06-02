package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 21 - File Upload
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T21FileUploadTest extends BaseTest {

    @Test(priority = 1, description = "NEG Upload image without file")
    public void NEG_Upload_image_without_file() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().post(url("{{baseUrl}}/api/upload/image"));
        assertStatus(r, 400, 500);
    }
}
