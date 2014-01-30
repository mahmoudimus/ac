package it;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.GeneralPageModule;
import it.servlet.ConnectAppServlets;
import org.apache.http.client.HttpResponseException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.atlassian.plugin.connect.test.Utils.getXml;
import static org.junit.Assert.*;

public class TestRemotePluginInstallation extends ConnectWebDriverTestBase
{
    public static final String EXTRA_PREFIX = "servlet-";
    
    @Test
    public void testSchemaContainsCustomScope() throws IOException, DocumentException
    {
        Document doc = getXml(product.getProductInstance().getBaseUrl() + "/rest/atlassian-connect/1/installer/schema/atlassian-plugin-remotable");
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
    public void testChangedKey() throws Exception
    {
        product.visit(LoginPage.class).login("admin", "admin", HomePage.class);
        AtlassianConnectAddOnRunner pluginFirst = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl(), "pluginFirst")
                .add(GeneralPageModule.key("changedPage")
                                      .name("Changed Page")
                                      .path("/page")
                                      .resource(ConnectAppServlets.helloWorldServlet()))
                .start();
        product.visit(HomePage.class);
        assertTrue(product.getPageBinder().bind(GeneralPage.class, "changedPage", "Changed Page",EXTRA_PREFIX)
                          .clickRemotePluginLink()
                          .isLoaded());
        pluginFirst.stopAndUninstall();

        AtlassianConnectAddOnRunner pluginSecond = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl(), "pluginSecond")
                .add(GeneralPageModule.key("changedPage")
                                      .name("Changed Page")
                                      .path("/page")
                                      .resource(ConnectAppServlets.helloWorldServlet()))
                .start();
        product.visit(HomePage.class);
        assertTrue(product.getPageBinder().bind(GeneralPage.class, "changedPage", "Changed Page")
                          .clickRemotePluginLink()
                          .isLoaded());
        pluginSecond.stopAndUninstall();
    }

    //TODO: JD fix this, need better trapping of errors during install
    @Ignore
    @Test(expected = HttpResponseException.class)
    public void testUnknownModuleAndFail() throws Exception
    {
        new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl(), "appFirst")
                .description("foo")
                .addUnknownModule("some-key")
                .start();
    }
}
