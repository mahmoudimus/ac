package it;

import com.atlassian.labs.remoteapps.test.MyIframePage;
import com.atlassian.labs.remoteapps.test.OAuthUtils;
import com.atlassian.labs.remoteapps.test.RemoteAppAwareAdminPage;
import com.atlassian.labs.remoteapps.test.OwnerOfTestedProduct;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.labs.remoteapps.test.Utils.getJson;
import static com.atlassian.labs.remoteapps.test.WebHookUtils.waitForEvent;
import static org.junit.Assert.assertEquals;
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
        MyIframePage myIframe = page.clickRemoteAppAdminLink();
        assertEquals("Success", myIframe.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), myIframe.getConsumerKey());
	}

    @Test
	public void testAppStartedWebHookFired() throws IOException, JSONException, InterruptedException
    {
        JSONObject event = waitForEvent(product.getProductInstance(), "remote_app_started");
        assertEquals("app1", event.getString("key"));
	}
}
