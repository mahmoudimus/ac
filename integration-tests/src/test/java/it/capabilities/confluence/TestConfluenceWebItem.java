package it.capabilities.confluence;

import com.atlassian.fugue.Pair;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import it.capabilities.CheckUsernameConditionServlet;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;
import java.net.URL;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static it.TestConstants.BARNEY_USERNAME;
import static it.TestConstants.BETTY_USERNAME;
import static it.capabilities.ConnectAsserts.assertURIEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * @since 1.0
 */
public class TestConfluenceWebItem extends ConfluenceWebDriverTestBase
{
    private static final String ADDON_WEBITEM = "ac-general-web-item";
    private static final String ADDON_DIRECT_WEBITEM = "ac-direct-to-addon-web-item";
    private static final String PRODUCT_WEBITEM = "quick-page-link";
    private static final String ABSOLUTE_WEBITEM = "google-link";
    private static final String SPACE = "ds";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addCapabilities("webItems",
                        newWebItemBean()
                                .withName(new I18nProperty("AC General Web Item", "ac.gen"))
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("/irwi?page_id={page.id}")
                                .build(),
                        newWebItemBean()
                                .withContext(AddOnUrlContext.addon)
                                .withName(new I18nProperty("AC Direct To Addon Web Item", "ac.dir"))
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("/irwi?page_id={page.id}")
                                .build(),
                        newWebItemBean()
                                .withContext(AddOnUrlContext.product)
                                .withName(new I18nProperty("Quick page link", "ac.qp"))
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("/pages/viewpage.action?pageId={page.id}")
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("google link", "ac.gl"))
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("http://www.google.com")
                                .withConditions(
                                        newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                        newSingleConditionBean().withCondition("/onlyBettyCondition").build()
                                )
                                .build())

                .addRoute("/onlyBarneyCondition", new CheckUsernameConditionServlet(BARNEY_USERNAME))
                .addRoute("/onlyBettyCondition", new CheckUsernameConditionServlet(BETTY_USERNAME))
                .addRoute("/irwi?page_id={page.id}", ConnectAppServlets.helloWorldServlet())
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
    public void testAbsoluteWebItem() throws Exception
    {
        loginAs(BETTY_USERNAME, BETTY_USERNAME);

        RemoteWebItem webItem = findViewPageWebItem(ABSOLUTE_WEBITEM).right();
        assertNotNull("Web item should be found", webItem);

        assertTrue("Web item link should be absolute", webItem.isPointingToACInternalUrl());
        assertURIEquals("http://www.google.com", webItem.getPath());
    }

    @Test
    public void testRelativeWebItem() throws Exception
    {
        loginAsAdmin();

        Pair<ConfluenceViewPage, RemoteWebItem> pageAndWebItem = findViewPageWebItem(ADDON_WEBITEM);
        RemoteWebItem webItem = pageAndWebItem.right();
        assertNotNull("Web item should be found", webItem);

        assertEquals(pageAndWebItem.left().getPageId(), webItem.getFromQueryString("page_id"));
        assertThat(webItem.getPath(), startsWith(product.getProductInstance().getBaseUrl()));
    }

    @Test
    public void testAddonDirectWebItem() throws Exception
    {
        loginAsAdmin();

        Pair<ConfluenceViewPage, RemoteWebItem> pageAndWebItem = findViewPageWebItem(ADDON_DIRECT_WEBITEM);
        RemoteWebItem webItem = pageAndWebItem.right();
        assertNotNull("Web item should be found", webItem);

        assertEquals(pageAndWebItem.left().getPageId(), webItem.getFromQueryString("page_id"));
        assertThat(webItem.getPath(), startsWith(remotePlugin.getAddon().getBaseUrl()));
    }

    @Test
    public void testProductWebItem() throws Exception
    {
        loginAsAdmin();

        ConfluenceViewPage viewPage = createAndVisitViewPage();

        RemoteWebItem webItem = viewPage.findWebItem(PRODUCT_WEBITEM, Optional.<String>of("action-menu-link"));
        assertNotNull("Web item should be found", webItem);

        webItem.click();

        URL url = new URL(webItem.getPath());
        assertThat(url.getPath(), is("/confluence/pages/viewpage.action"));
        assertEquals(viewPage.getPageId(), webItem.getFromQueryString("pageId"));
    }

    @Test
    public void bettyCanSeeWebItem() throws Exception
    {
        loginAs(BETTY_USERNAME, BETTY_USERNAME);

        RemoteWebItem webItem = findViewPageWebItem(ABSOLUTE_WEBITEM).right();

        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyWebItem() throws Exception
    {
        loginAsAdmin();
        ConfluenceViewPage viewPage = createAndVisitViewPage();
        assertTrue("Web item should NOT be found", viewPage.webItemDoesNotExist(ABSOLUTE_WEBITEM));
    }

    private Pair<ConfluenceViewPage, RemoteWebItem> findViewPageWebItem(String webItemId) throws Exception
    {
        ConfluenceViewPage viewPage = createAndVisitViewPage();
        return Pair.pair(viewPage, viewPage.findWebItem(webItemId, Optional.<String>absent()));
    }

    private ConfluenceViewPage createAndVisitViewPage() throws Exception
    {
        return createAndVisitPage(ConfluenceViewPage.class);
    }


    private <P extends Page> P createAndVisitPage(Class<P> pageClass) throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = createPage();
        return product.visit(pageClass, pageData.getId());
    }

    private ConfluenceOps.ConfluencePageData createPage() throws MalformedURLException, XmlRpcFault
    {
        return confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), SPACE, "Page with webitem", "some page content");
    }


    //TODO: once generalPage is complete, add a test to check that a web item pointing to the page works properly


}
