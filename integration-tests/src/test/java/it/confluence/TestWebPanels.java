package it.confluence;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceEditPage;
import com.atlassian.plugin.remotable.test.pageobjects.RemoteWebPanel;
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
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

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
                        .path("/eswp?page_id=${page.id}&space_id=${space.id}&space_key=${space.key}")
                        .location("atl.editor")
                        .resource(newServlet(new MyContextAwareWebPanelServlet())))
                .add(RemoteWebPanelModule.key("edit-screen-web-panel-2")
                        .name("Remotable Edit Screen Web Panel 2")
                        .path("/eswp2?my-page-id=${page.id}&my-space-id=${space.id}")
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
        final String pageId = pageData.getId();
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);
        ConfluenceEditPage editPage = product.visit(ConfluenceEditPage.class, pageId);
        RemoteWebPanel webPanel = editPage.findWebPanel("edit-screen-web-panel");

        assertEquals(pageId, webPanel.getPageId());
        // Confluence doesn't provide space id via the xml-rpc API, so we can't find the actual space id.
        assertNotNull(webPanel.getSpaceId());
        assertEquals("ds", webPanel.getFromQueryString("space_key"));
        assertEquals(BETTY, webPanel.getUserId());
    }

    @Test
    public void testRemoteWebPanelOnEditPageArbitraryData() throws MalformedURLException, XmlRpcFault
    {
        final ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), "ds", "Page with webpanel", "some page content");
        final String pageId = pageData.getId();
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);
        ConfluenceEditPage editPage = product.visit(ConfluenceEditPage.class, pageId);
        RemoteWebPanel webPanel = editPage.findWebPanel("edit-screen-web-panel-2");

        assertEquals(pageId, webPanel.getFromQueryString("my-page-id"));
        // Confluence doesn't provide space id via the xml-rpc API, so we can't find the actual space id.
        assertNotNull(webPanel.getFromQueryString("my-space-id"));
    }
}
