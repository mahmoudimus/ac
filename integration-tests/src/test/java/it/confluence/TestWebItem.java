package it.confluence;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.RemoteWebItemModule;
import com.google.common.base.Optional;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static com.atlassian.fugue.Option.some;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

/**
 * Test of remote web items in Confluence.
 */
@XmlDescriptor
public class TestWebItem extends ConfluenceWebDriverTestBase
{
    private static final String GENERAL_WEBITEM = "system-web-item";
    private static final String ABSOLUTE_WEB_ITEM = "absolute-web-item";

    private static AtlassianConnectAddOnRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .add(RemoteWebItemModule.key(GENERAL_WEBITEM)
                        .name("AC General Web Item")
                        .section("system.browse")
                        .weight(100)
                        .link(RemoteWebItemModule.Link.link("/irwi?space_id=${space.key}&page_id=${page.id}", false))
                        .resource(ConnectAppServlets.helloWorldServlet()))
                .add(RemoteWebItemModule.key(ABSOLUTE_WEB_ITEM)
                        .name("Quick project link")
                        .section("system.browse")
                        .weight(100)
                        .link(RemoteWebItemModule.Link.link(product.getProductInstance().getBaseUrl() + "/display/${space.key}", false)))
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
    public void testWebItemWithSpaceInContext() throws RemoteException, MalformedURLException, XmlRpcFault
    {
        final ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), "ds", "Page with webpanel", "some page content");
        final String pageId = pageData.getId();
        loginAsBetty();
        ConfluenceEditPage editPage = product.visit(ConfluenceEditPage.class, pageId);

        RemoteWebItem webItem = editPage.findWebItem(GENERAL_WEBITEM, Optional.of("help-menu-link"));
        assertNotNull("Web item should be found", webItem);
        assertFalse("Web item link shouldn't be absolute", webItem.isPointingToOldXmlInternalUrl());

        webItem.click();

        assertEquals("ds", webItem.getFromQueryString("space_id"));
        assertEquals(pageData.getId(), webItem.getFromQueryString("page_id"));
    }

    @Test
    public void testAbsoluteWebItemWithContext() throws RemoteException, MalformedURLException, XmlRpcFault
    {
        final ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), "ds", "Page with webpanel", "some page content");
        final String pageId = pageData.getId();
        loginAsBetty();
        ConfluenceEditPage editPage = product.visit(ConfluenceEditPage.class, pageId);

        RemoteWebItem webItem = editPage.findWebItem(ABSOLUTE_WEB_ITEM, Optional.of("help-menu-link"));
        assertNotNull("Web item should be found", webItem);
        assertTrue("Web item link should be absolute", webItem.isPointingToOldXmlInternalUrl());

        webItem.click();

        assertThat(webItem.getPath(), containsString("display/ds"));
    }
}
