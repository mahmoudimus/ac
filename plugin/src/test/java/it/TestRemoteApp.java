package it;

import com.atlassian.labs.remoteapps.test.*;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import java.io.IOException;
import java.util.List;

import static com.atlassian.labs.remoteapps.test.Utils.getXml;
import static com.atlassian.labs.remoteapps.test.RemoteAppUtils.waitForEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestRemoteApp
{
    private static TestedProduct<WebDriverTester> product = OwnerOfTestedProduct.INSTANCE;

    @Rule
    public MethodRule rule = new HtmlDumpRule(product.getTester().getDriver());

    @After
    public void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    @Test
	public void testMyAdminLoaded()
	{
        product.visit(LoginPage.class).login("betty", "betty", AdminHomePage.class);
        RemoteAppAwarePage page = product.getPageBinder().bind(RemoteAppAwarePage.class, "remoteAppAdmin",
                                                               "Remote App app1 Admin");
        assertTrue(page.isRemoteAppLinkPresent());
        MyIframePage myIframe = page.clickRemoteAppLink();
        assertEquals("Success", myIframe.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), myIframe.getConsumerKey());
	}

    @Test
	public void testAppStartedWebHookFired() throws IOException, JSONException, InterruptedException
    {
        JSONObject event = waitForEvent(product.getProductInstance(), "remote_app_started");
        assertEquals("app1", event.getString("key"));
	}

    @Test
    public void testSchemaContainsCustomScope() throws IOException, DocumentException
    {
        Document doc = getXml(product.getProductInstance().getBaseUrl() + "/rest/remoteapps/1/installer/schema/remote-app");
        Element documentation = (Element) doc.selectSingleNode("//xs:enumeration[@value='resttest']/xs:annotation/xs:documentation");

        assertNotNull(documentation);
        assertEquals("Rest Test", documentation.element("name").getTextTrim());
        assertEquals("A test resource", documentation.element("description").getTextTrim());
        List<Element> resources = documentation.element("resources").elements("resource");
        assertEquals("/rest/remoteapptest/latest/", resources.get(0).attributeValue("path"));
        assertEquals("GET", resources.get(0).attributeValue("httpMethod"));
        assertEquals("/rest/remoteapptest/1/", resources.get(1).attributeValue("path"));
        assertEquals("GET", resources.get(1).attributeValue("httpMethod"));
    }

    @Test
    public void testChangedKey() throws Exception
    {
        product.visit(LoginPage.class).login("betty", "betty", AdminHomePage.class);
        RemoteAppRunner appFirst = new RemoteAppRunner(product.getProductInstance().getBaseUrl(), "appFirst")
                .addAdminPage("changedPage", "Changed Page", "/page", "hello-world-page.mu")
                .start();
        product.visit(AdminHomePage.class);
        assertTrue(product.getPageBinder().bind(RemoteAppAwarePage.class, "changedPage", "Changed Page")
                .clickRemoteAppLink()
                .getLoadTime() > 0);
        appFirst.stop();

        RemoteAppRunner appSecond = new RemoteAppRunner(product.getProductInstance().getBaseUrl(), "appSecond")
                .addAdminPage("changedPage", "Changed Page", "/page", "hello-world-page.mu")
                .start();
        product.visit(AdminHomePage.class);
        assertTrue(product.getPageBinder().bind(RemoteAppAwarePage.class, "changedPage", "Changed Page")
                .clickRemoteAppLink()
                .getLoadTime() > 0);
        appSecond.stop();
    }
}
