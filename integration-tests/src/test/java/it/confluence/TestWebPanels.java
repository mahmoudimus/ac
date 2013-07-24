package it.confluence;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceEditPage;
import com.atlassian.plugin.remotable.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.remotable.test.server.module.RemoteWebPanelModule;
import it.MyContextAwareWebPanelServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;
import java.util.Map;

import static com.atlassian.plugin.remotable.test.Utils.createSignedRequestHandler;
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
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl(), "app1")
                .addOAuth(createSignedRequestHandler("app1"))
                .addPermission(Permissions.CREATE_OAUTH_LINK)
                .add(RemoteWebPanelModule.key("edit-screen-web-panel")
                        .name("Remotable Edit Screen Web Panel")
                        .path("/eswp?page_id=${page.id}&space_id=${space.id}")
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
        final Map pageData = confluenceOps.setPage("ds", "Page with webpanel", "some page content");
        final String pageId = (String) pageData.get("id");
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);
        ConfluenceEditPage editPage = product.visit(ConfluenceEditPage.class, pageId);
        RemoteWebPanel webPanel = editPage.findWebPanel("edit-screen-web-panel");

        assertEquals(pageId, webPanel.getPageId());
        // Confluence doesn't provide space id via the xml-rpc API, so we can't find the actual space id.
        assertNotNull(webPanel.getSpaceId());
        assertEquals(BETTY, webPanel.getUserId());
    }

    @Test
    public void testRemoteWebPanelOnEditPageArbitraryData() throws MalformedURLException, XmlRpcFault
    {
        final Map pageData = confluenceOps.setPage("ds", "Page with webpanel", "some page content");
        final String pageId = (String) pageData.get("id");
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);
        ConfluenceEditPage editPage = product.visit(ConfluenceEditPage.class, pageId);
        RemoteWebPanel webPanel = editPage.findWebPanel("edit-screen-web-panel-2");

        assertEquals(pageId, webPanel.getFromQueryString("my-page-id"));
        // Confluence doesn't provide space id via the xml-rpc API, so we can't find the actual space id.
        assertNotNull(webPanel.getFromQueryString("my-space-id"));
    }
}
