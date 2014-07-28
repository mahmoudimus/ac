package it.confluence;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.test.OAuthUtils;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluencePageMacroPage;
import com.atlassian.plugin.connect.test.server.XMLAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.MacroPageModule;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.io.IOException;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.test.Utils.loadResourceAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@XmlDescriptor
public final class TestConfluencePageMacro extends ConfluenceWebDriverTestBase
{
    private static XMLAddOnRunner remotePlugin;

    @BeforeClass
    public static void setupConfluenceAndStartConnectAddOn() throws Exception
    {
        remotePlugin = new XMLAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .addPermission("read_content")
                .addPermission("read_users_and_groups")
                .addPermission("read_server_information")
                .add(MacroPageModule.key("app1-page")
                                    .name("app1-page")
                                    .path("/ap")
                                    .outputType("block")
                                    .bodyType("none")
                                    .resource(ConnectAppServlets.apRequestServlet()))
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void testPageMacro() throws XmlRpcFault, IOException
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(TestUser.ADMIN), "ds", "testPageMacro", loadResourceAsString("confluence/test-page-macro.xhtml"));
        ConfluencePageMacroPage page = loginAndVisit(TestUser.BETTY, ConfluencePageMacroPage.class, pageData.getTitle(), "app1-page-0");

        assertFalse(page.getContainerDiv().getAttribute("class").contains("ap-inline"));
        assertEquals("Success", page.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), page.getConsumerKey());
    }

    @Test
    public void testPageMacroMultipleImplementations() throws XmlRpcFault, IOException
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(TestUser.ADMIN), "ds", "testMultiMacro", loadResourceAsString("confluence/test-page-macro-multiple.xhtml"));

        ConfluencePageMacroPage iframe1 = loginAndVisit(TestUser.BETTY, ConfluencePageMacroPage.class, pageData.getTitle(), "app1-page-0");
        assertEquals("Success", iframe1.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), iframe1.getConsumerKey());

        ConfluencePageMacroPage iframe2 = product.visit(ConfluencePageMacroPage.class, pageData.getTitle(), "app1-page-1");

        assertEquals("Success", iframe2.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), iframe2.getConsumerKey());
    }
}
