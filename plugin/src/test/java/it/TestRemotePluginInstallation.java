package it;

import com.atlassian.plugin.remotable.test.GeneralPage;
import com.atlassian.plugin.remotable.test.RemotePluginRunner;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import org.apache.http.client.HttpResponseException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.atlassian.plugin.remotable.test.Utils.getXml;
import static org.junit.Assert.*;

public class TestRemotePluginInstallation extends AbstractRemotablePluginTest
{
    @Test
    public void testSchemaContainsCustomScope() throws IOException, DocumentException
    {
        Document doc = getXml(product.getProductInstance().getBaseUrl() + "/rest/remotable-plugins/1/installer/schema/atlassian-plugin");
        Element documentation = (Element) doc.selectSingleNode("//xs:enumeration[@value='resttest']/xs:annotation/xs:documentation");

        assertNotNull(documentation);
        assertEquals("Rest Test", documentation.element("name").getTextTrim());
        assertEquals("A test resource", documentation.element("description").getTextTrim());
        List<Element> resources = documentation.element("resources").elements("resource");
        assertEquals("/rest/remoteplugintest/latest/user", resources.get(0).attributeValue("path"));
        assertEquals("GET", resources.get(0).attributeValue("httpMethod"));
        assertEquals("/rest/remoteplugintest/1/user", resources.get(1).attributeValue("path"));
        assertEquals("GET", resources.get(1).attributeValue("httpMethod"));
    }

    @Test
    public void testInstalledPluginWithSecret() throws Exception
    {
        product.visit(LoginPage.class).login("admin", "admin", HomePage.class);
        RemotePluginRunner pluginFirst = new RemotePluginRunner(product.getProductInstance().getBaseUrl(), "installed")
                .addGeneralPage("page", "Page", "/page", "hello-world-page.mu")
                .secret("secret")
                .start();
        product.visit(HomePage.class);
        assertTrue(product.getPageBinder().bind(GeneralPage.class, "page", "Page")
                .clickRemotePluginLink()
                .getLoadTime() > 0);
        pluginFirst.stop();
    }

    @Test
    public void testChangedKey() throws Exception
    {
        product.visit(LoginPage.class).login("admin", "admin", HomePage.class);
        RemotePluginRunner pluginFirst = new RemotePluginRunner(product.getProductInstance().getBaseUrl(), "pluginFirst")
                .addGeneralPage("changedPage", "Changed Page", "/page", "hello-world-page.mu")
                .start();
        product.visit(HomePage.class);
        assertTrue(product.getPageBinder().bind(GeneralPage.class, "changedPage", "Changed Page")
                .clickRemotePluginLink()
                .getLoadTime() > 0);
        pluginFirst.stop();

        RemotePluginRunner pluginSecond = new RemotePluginRunner(product.getProductInstance().getBaseUrl(), "pluginSecond")
                .addGeneralPage("changedPage", "Changed Page", "/page", "hello-world-page.mu")
                .start();
        product.visit(HomePage.class);
        assertTrue(product.getPageBinder().bind(GeneralPage.class, "changedPage", "Changed Page")
                .clickRemotePluginLink()
                .getLoadTime() > 0);
        pluginSecond.stop();
    }

    @Test(expected = HttpResponseException.class)
    public void testUnknownModuleAndFail() throws Exception
    {
        new RemotePluginRunner(product.getProductInstance().getBaseUrl(), "appFirst")
                .description("foo")
                .addUnknownModule("some-key")
                .start();
    }
}
