package it;

import com.atlassian.labs.remoteapps.test.MyAdminPage;
import com.atlassian.labs.remoteapps.test.OAuthUtils;
import com.atlassian.labs.remoteapps.test.MyAdminAccessDeniedPage;
import com.atlassian.labs.remoteapps.test.RemoteAppAwareAdminPage;
import com.atlassian.labs.remoteapps.test.OwnerOfTestedProduct;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestRemoteApp
{
    private static TestedProduct<?> product = OwnerOfTestedProduct.INSTANCE;

    @After
    public void logout()
    {
        ((WebDriverTester)product.getTester()).getDriver().manage().deleteAllCookies();
    }

    @Test
	public void testMyAdminLoaded()
	{
        product.visit(LoginPage.class).login("betty", "betty", AdminHomePage.class);
        RemoteAppAwareAdminPage page = product.getPageBinder().bind(RemoteAppAwareAdminPage.class);
        assertTrue(page.isRemoteAppLinkPresent());
        MyAdminPage myAdmin = page.clickRemoteAppAdminLink();
        assertEquals("Success", myAdmin.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), myAdmin.getConsumerKey());
        assertEquals("betty", myAdmin.getRemoteUsername());
        assertEquals("403", myAdmin.getForbiddenApiStatusCode());
	}

    @Test
	public void testMyAdminBockedForSysAdmin()
	{
        MyAdminAccessDeniedPage page = product
                .visit(LoginPage.class)
                .loginAsSysAdmin(MyAdminAccessDeniedPage.class);
        assertNotNull(page.getMessage());
	}
}
