package it.confluence;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceEditPage;
import com.atlassian.plugin.remotable.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.remotable.test.pageobjects.RemoteWebPanels;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.remotable.test.server.module.RemoteWebPanelModule;
import it.MyContextAwareWebPanelServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner.newServlet;
import static it.TestConstants.BETTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test of remote web panels in Confluence.
 */
public class TestWebPanels extends ConfluenceWebDriverTestBase
{
    private static AtlassianConnectAddOnRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .add(RemoteWebPanelModule.key("edit-screen-web-panel")
                        .name("Remotable Edit Screen Web Panel")
                        .path("/eswp")
                        .location("atl.editor")
                        .resource(newServlet(new MyContextAwareWebPanelServlet())))
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stop();
        }
    }

    @Test
    public void testRemoteWebPanelOnEditPage() throws MalformedURLException, XmlRpcFault
    {
        final ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), "ds", "Page with webpanel", "some page content");
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);
        ConfluenceEditPage editPage = product.visit(ConfluenceEditPage.class, pageData.getId());
        RemoteWebPanels webPanels = editPage.getWebPanels();
        assertNotNull("Remote web panels should be found", webPanels);
        final RemoteWebPanel webPanel = webPanels.getWebPanel("edit-screen-web-panel");
        assertNotNull("Panel should be found", webPanel);
        assertEquals(pageData.getId(), webPanel.getPageId());
        assertEquals(BETTY, webPanel.getUserId());
    }
}
