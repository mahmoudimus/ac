package it.confluence;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test of remote web items in Confluence.
 */
public class TestWebItem extends ConfluenceWebDriverTestBase
{
    private static final String GENERAL_WEBITEM = "system-web-item";
    private static final String ABSOLUTE_WEBITEM = "absolute-web-item";
    private static final String SPACE_KEY = "ds";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules(
                        "webItems",
                        newWebItemBean()
                                .withName(new I18nProperty("General Web Item", null))
                                .withKey(GENERAL_WEBITEM)
                                .withUrl("/web-item?space_id=${space.key}&page_id=${page.id}")
                                .withLocation("system.browse")
                                .withWeight(100)
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("Absolute Web Item", null))
                                .withKey(ABSOLUTE_WEBITEM)
                                .withUrl(product.getProductInstance().getBaseUrl() + "/display/${space.key}")
                                .withLocation("system.browse")
                                .withWeight(100)
                                .build()
                )
                .addRoute("/web-item", ConnectAppServlets.helloWorldServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void testWebItemWithSpaceInContext() throws RemoteException, MalformedURLException, XmlRpcFault
    {
        final ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(ConfluenceOps.ConfluenceUser.ADMIN), "ds", "Page with webpanel", "some page content");
        final String pageId = pageData.getId();
        loginAsBetty();

        product.visit(ConfluenceEditPage.class, pageId);

        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(runner.getAddon().getKey(), GENERAL_WEBITEM), Optional.of("help-menu-link"));
        assertNotNull("Web item should be visible", webItem);
        assertTrue("Web item link should point to add-on base", webItem.getPath().startsWith(runner.getAddon().getBaseUrl()));

        webItem.click();

        assertEquals("ds", webItem.getFromQueryString("space_id"));
        assertEquals(pageData.getId(), webItem.getFromQueryString("page_id"));
    }

    @Test
    public void testAbsoluteWebItemWithContext() throws RemoteException, MalformedURLException, XmlRpcFault
    {
        final ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(ConfluenceOps.ConfluenceUser.ADMIN), SPACE_KEY, "Page with webpanel", "some page content");
        final String pageId = pageData.getId();
        loginAsBetty();
        product.visit(ConfluenceEditPage.class, pageId);

        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(runner.getAddon().getKey(), ABSOLUTE_WEBITEM), Optional.of("help-menu-link"));
        assertNotNull("Web item should be visible", webItem);
        assertTrue("Web item link should point to product base", webItem.getPath().startsWith(product.getProductInstance().getBaseUrl()));

        webItem.click();

        assertThat(webItem.getPath(), containsString("display/" + SPACE_KEY));
    }
}
