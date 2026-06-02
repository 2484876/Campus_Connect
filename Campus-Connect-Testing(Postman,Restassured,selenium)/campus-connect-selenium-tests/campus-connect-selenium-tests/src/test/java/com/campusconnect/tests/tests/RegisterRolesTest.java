package com.campusconnect.tests.tests;

import com.campusconnect.tests.base.BaseTest;
import com.campusconnect.tests.pages.GenericPage;
import com.campusconnect.tests.pages.RegisterPage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** The 12 designation options exist and are selectable. Group "smoke". */
public class RegisterRolesTest extends BaseTest {

    @DataProvider(name = "roles")
    public Object[][] roles() {
        return new Object[][] {
            {"PROGRAMMER_ANALYST_TRAINEE"}, {"PROGRAMMER_ANALYST"}, {"ASSOCIATE"},
            {"SENIOR_ASSOCIATE"}, {"MANAGER"}, {"SENIOR_MANAGER"}, {"ASSOCIATE_DIRECTOR"},
            {"DIRECTOR"}, {"SENIOR_DIRECTOR"}, {"AVP"}, {"VP"}, {"SVP"}
        };
    }

    @Test(groups = "smoke", dataProvider = "roles", description = "Designation option exists")
    public void optionExists(String value) {
        new RegisterPage(driver).openPage();
        GenericPage gp = new GenericPage(driver);
        assertEquals(gp.howMany(RegisterPage.roleOption(value)), 1, "Missing option: " + value);
    }

    @Test(groups = "smoke", dataProvider = "roles", description = "Designation option is selectable")
    public void optionSelectable(String value) {
        RegisterPage reg = new RegisterPage(driver).openPage();
        reg.selectRoleByValue(value);
        assertEquals(reg.selectedRoleValue(), value);
    }
}
