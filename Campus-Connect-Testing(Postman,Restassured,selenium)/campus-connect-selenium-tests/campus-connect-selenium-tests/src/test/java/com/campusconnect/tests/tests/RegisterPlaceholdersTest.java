package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseTest;
import com.campusconnect.tests.pages.GenericPage;
import com.campusconnect.tests.pages.RegisterPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.openqa.selenium.By;

import static org.testng.Assert.assertEquals;

/** Each register field shows its expected placeholder. Group "smoke". */
public class RegisterPlaceholdersTest extends BaseTest {

    private GenericPage gp;

    @BeforeMethod(alwaysRun = true)
    public void open() { new RegisterPage(driver).openPage(); gp = new GenericPage(driver); }

    @DataProvider(name = "fields")
    public Object[][] fields() {
        return new Object[][] {
            {RegisterPage.NAME, "Rahul Kumar"},
            {RegisterPage.EMAIL, "rahul@cognizant.com"},
            {RegisterPage.PASSWORD, "Create a password"},
            {RegisterPage.DEPARTMENT, "Digital Engineering"},
            {RegisterPage.POSITION, "Full Stack Developer"},
        };
    }

    @Test(groups = "smoke", dataProvider = "fields", description = "Field placeholder is correct")
    public void placeholder(By field, String expected) {
        assertEquals(gp.attrOf(field, "placeholder"), expected);
    }
}
