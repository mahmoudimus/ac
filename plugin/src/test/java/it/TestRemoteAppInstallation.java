package it;

import com.atlassian.labs.remoteapps.test.GeneralPage;
import com.atlassian.labs.remoteapps.test.RemoteAppRunner;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import org.apache.http.client.HttpResponseException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.atlassian.labs.remoteapps.test.Utils.getXml;
import static org.junit.Assert.*;

public class TestRemoteAppInstallation extends AbstractRemoteAppTest
{
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
        product.visit(LoginPage.class).login("admin", "admin", HomePage.class);
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
        product.visit(LoginPage.class).login("admin", "admin", HomePage.class);
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
}
