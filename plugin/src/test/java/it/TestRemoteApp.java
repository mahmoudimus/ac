package it;

import com.atlassian.labs.remoteapps.test.MyAdminPage;
import com.atlassian.labs.remoteapps.test.OAuthUtils;
import com.atlassian.labs.remoteapps.test.RemoteAppAwareAdminPage;
import com.atlassian.labs.remoteapps.test.OwnerOfTestedProduct;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestRemoteApp
{
    private static TestedProduct<?> product = OwnerOfTestedProduct.INSTANCE;

    @Before
    public void login()
    {
        product.visit(LoginPage.class).loginAsSysAdmin(AdminHomePage.class);
    }

    @After
    public void logout()
    {
        ((WebDriverTester)product.getTester()).getDriver().manage().deleteAllCookies();
    }

    @Test
	public void testMyAdminLoaded()
	{
        RemoteAppAwareAdminPage page = product.getPageBinder().bind(RemoteAppAwareAdminPage.class);
        assertTrue(page.isRemoteAppLinkPresent());
        MyAdminPage myAdmin = page.clickRemoteAppAdminLink();
        assertEquals("Success", myAdmin.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), myAdmin.getConsumerKey());

	}
}
