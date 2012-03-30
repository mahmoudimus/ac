package it;

import com.atlassian.labs.remoteapps.test.*;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.apache.http.client.HttpResponseException;
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
import static org.junit.Assert.*;

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
	public void testMyGeneralLoaded()
	{
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        RemoteAppAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remoteAppGeneral",
                                                               "Remote App app1 General");
        assertTrue(page.isRemoteAppLinkPresent());
        RemoteAppTestPage remoteAppTest = page.clickRemoteAppLink();
        assertEquals("Success", remoteAppTest.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), remoteAppTest.getConsumerKey());
        assertEquals("Betty Admin", remoteAppTest.getFullName());
	}

    @Test
    public void testLoadGeneralDialog()
    {
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        RemoteAppAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remoteAppDialog",
                "Remote App app1 Dialog");
        assertTrue(page.isRemoteAppLinkPresent());
        RemoteAppTestPage remoteAppTest = page.clickRemoteAppLink();
        assertEquals("Success", remoteAppTest.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), remoteAppTest.getConsumerKey());
        assertEquals("Betty Admin", remoteAppTest.getFullName());
    }

    @Test
    public void testNoAdminPageForNonAdmin()
    {
        product.visit(LoginPage.class).login("barney", "barney", AdminHomePage.class);
        AccessDeniedIFramePage page = product.getPageBinder().bind(AccessDeniedIFramePage.class,
                "app1", "remoteAppAdmin");
        assertFalse(page.isIframeAvailable());
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
        assertEquals("/rest/remoteapptest/latest/user", resources.get(0).attributeValue("path"));
        assertEquals("GET", resources.get(0).attributeValue("httpMethod"));
        assertEquals("/rest/remoteapptest/1/user", resources.get(1).attributeValue("path"));
        assertEquals("GET", resources.get(1).attributeValue("httpMethod"));
    }

    @Test
    public void testInstalledAppWithSecret() throws Exception
    {
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        RemoteAppRunner appFirst = new RemoteAppRunner(product.getProductInstance().getBaseUrl(), "installed")
                .addGeneralPage("page", "Page", "/page", "hello-world-page.mu")
                .secret("secret")
                .start();
        product.visit(HomePage.class);
        assertTrue(product.getPageBinder().bind(GeneralPage.class, "page", "Page")
                .clickRemoteAppLink()
                .getLoadTime() > 0);
        appFirst.stop();
    }

    @Test
    public void testChangedKey() throws Exception
    {
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        RemoteAppRunner appFirst = new RemoteAppRunner(product.getProductInstance().getBaseUrl(), "appFirst")
                .addGeneralPage("changedPage", "Changed Page", "/page", "hello-world-page.mu")
                .start();
        product.visit(HomePage.class);
        assertTrue(product.getPageBinder().bind(GeneralPage.class, "changedPage", "Changed Page")
                .clickRemoteAppLink()
                .getLoadTime() > 0);
        appFirst.stop();

        RemoteAppRunner appSecond = new RemoteAppRunner(product.getProductInstance().getBaseUrl(), "appSecond")
                .addGeneralPage("changedPage", "Changed Page", "/page", "hello-world-page.mu")
                .start();
        product.visit(HomePage.class);
        assertTrue(product.getPageBinder().bind(GeneralPage.class, "changedPage", "Changed Page")
                .clickRemoteAppLink()
                .getLoadTime() > 0);
        appSecond.stop();
    }

    @Test(expected = HttpResponseException.class)
    public void testUnknownModuleAndFail() throws Exception
    {
        new RemoteAppRunner(product.getProductInstance().getBaseUrl(), "appFirst")
                .description("foo")
                .addUnknownModule("some-key")
                .start();
    }

    @Test
    public void testUnknownModuleAndPass() throws Exception
    {
        new RemoteAppRunner(product.getProductInstance().getBaseUrl(), "appFirst")
                .addUnknownModule("some-key")
                .description("foo")
                .stripUnknownModules()
                .start();
    }
}
