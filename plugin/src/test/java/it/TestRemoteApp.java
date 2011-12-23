package it;

import com.atlassian.labs.remoteapps.test.MyIframePage;
import com.atlassian.labs.remoteapps.test.OAuthUtils;
import com.atlassian.labs.remoteapps.test.RemoteAppAwareAdminPage;
import com.atlassian.labs.remoteapps.test.OwnerOfTestedProduct;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jdom.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static com.atlassian.labs.remoteapps.test.Utils.getJson;
import static com.atlassian.labs.remoteapps.test.Utils.getXml;
import static com.atlassian.labs.remoteapps.test.WebHookUtils.waitForEvent;
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
}
