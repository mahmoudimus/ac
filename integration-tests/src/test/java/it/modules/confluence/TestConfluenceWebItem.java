package it.modules.confluence;

import com.atlassian.fugue.Pair;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.CheckUsernameConditionServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;
import java.net.URL;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static it.TestConstants.BARNEY_USERNAME;
import static it.TestConstants.BETTY_USERNAME;
import static it.matcher.IsInteger.isInteger;
import static it.modules.ConnectAsserts.verifyStandardAddOnRelativeQueryParameters;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;


/**
 * @since 1.0
 */
public class TestConfluenceWebItem extends ConfluenceWebDriverTestBase
{
    private static final String ADDON_WEBITEM = "ac-general-web-item";
    private static final String ADDON_DIRECT_WEBITEM = "ac-direct-to-addon-web-item";
    private static final String PRODUCT_WEBITEM = "quick-page-link";
    private static final String ABSOLUTE_WEBITEM = "google-link";
    private static final String ADDON_WEBITEM_INLINE_DIALOG = "wikipedia-link";
    private static final String SPACE = "ds";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("webItems",
                        newWebItemBean()
                                .withName(new I18nProperty("AC General Web Item", "ac.gen"))
                                .withKey(ADDON_WEBITEM)
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("/irwi?page_id={page.id}")
                                .build(),
                        newWebItemBean()
                                .withContext(AddOnUrlContext.addon)
                                .withName(new I18nProperty("AC Direct To Addon Web Item", "ac.dir"))
                                .withKey(ADDON_DIRECT_WEBITEM)
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("/irwi?page_id={page.id}")
                                .build(),
                        newWebItemBean()
                                .withContext(AddOnUrlContext.product)
                                .withName(new I18nProperty("Quick page link", "ac.qp"))
                                .withKey(PRODUCT_WEBITEM)
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("/pages/viewpage.action?pageId={page.id}")
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("google link", "ac.gl"))
                                .withKey(ABSOLUTE_WEBITEM)
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("http://www.google.com?myPageId={page.id}&mySpaceKey={space.key}")
                                .withConditions(
                                        newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                        newSingleConditionBean().withCondition("/onlyBettyCondition").build()
                                ).build(),
                        newWebItemBean()
                                .withName(new I18nProperty("wikipedia link", "ac.ild"))
                                .withKey(ADDON_WEBITEM_INLINE_DIALOG)
                                .withLocation("system.content.metadata")
                                .withWeight(1)
                                .withContext(AddOnUrlContext.addon)
                                .withUrl("http://www.wikipedia.org")
                                .withTarget(
                                        newWebItemTargetBean().withType(WebItemTargetType.inlineDialog).build()
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
        loginAsBetty();

        RemoteWebItem webItem = findViewPageWebItem(getModuleKey(ABSOLUTE_WEBITEM)).right();
        assertNotNull("Web item should be found", webItem);

        assertTrue("Web item link should be absolute", webItem.isPointingToACInternalUrl());
        assertThat(webItem.getPath(), startsWith("http://www.google.com/?"));
        assertThat(webItem.getFromQueryString("myPageId"), isInteger());
        assertThat(webItem.getFromQueryString("mySpaceKey"), equalTo("ds"));
    }

    @Test
    public void testRelativeWebItem() throws Exception
    {
        loginAsAdmin();

        Pair<ConfluenceViewPage, RemoteWebItem> pageAndWebItem = findViewPageWebItem(getModuleKey(ADDON_WEBITEM));
        RemoteWebItem webItem = pageAndWebItem.right();
        assertNotNull("Web item should be found", webItem);

        assertEquals(pageAndWebItem.left().getPageId(), webItem.getFromQueryString("page_id"));
        // web-item url mode is relative to the addon by default
        assertThat(webItem.getPath(), startsWith(remotePlugin.getAddon().getBaseUrl()));

        verifyStandardAddOnRelativeQueryParameters(webItem, "/confluence");
    }

    @Test
    public void testAddonDirectWebItem() throws Exception
    {
        loginAsAdmin();

        Pair<ConfluenceViewPage, RemoteWebItem> pageAndWebItem = findViewPageWebItem(getModuleKey(ADDON_DIRECT_WEBITEM));
        RemoteWebItem webItem = pageAndWebItem.right();
        assertNotNull("Web item should be found", webItem);

        assertEquals(pageAndWebItem.left().getPageId(), webItem.getFromQueryString("page_id"));
        assertThat(webItem.getPath(), startsWith(remotePlugin.getAddon().getBaseUrl()));

        verifyStandardAddOnRelativeQueryParameters(webItem, "/confluence");
    }

    @Test
    public void testProductWebItem() throws Exception
    {
        loginAsAdmin();

        ConfluenceViewPage viewPage = createAndVisitViewPage();

        RemoteWebItem webItem = viewPage.findWebItem(getModuleKey(PRODUCT_WEBITEM), Optional.<String>of("action-menu-link"));
        assertNotNull("Web item should be found", webItem);

        webItem.click();

        URL url = new URL(webItem.getPath());
        assertThat(url.getPath(), is("/confluence/pages/viewpage.action"));
        assertEquals(viewPage.getPageId(), webItem.getFromQueryString("pageId"));
    }

    @Test
    public void bettyCanSeeWebItem() throws Exception
    {
        loginAsBetty();

        RemoteWebItem webItem = findViewPageWebItem(getModuleKey(ABSOLUTE_WEBITEM)).right();

        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyWebItem() throws Exception
    {
        loginAsAdmin();
        ConfluenceViewPage viewPage = createAndVisitViewPage();
        assertFalse("Web item should NOT be found", viewPage.existsWebItem(getModuleKey(ABSOLUTE_WEBITEM)));
    }


    @Test
    public void testAddonWebItemInlineDialog() throws Exception
    {
        loginAsAdmin();

        Pair<ConfluenceViewPage, RemoteWebItem> pageAndWebItem = findViewPageWebItem(getModuleKey(ADDON_WEBITEM_INLINE_DIALOG));
        RemoteWebItem webItem = pageAndWebItem.right();
        assertNotNull("Web item should be found", webItem);
        assertTrue("web item should be an inline dialog", webItem.isInlineDialog());
        webItem.click();
        assertTrue("web item inline dialog should be open", webItem.isActiveInlineDialog());
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


    private String getModuleKey(String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(),module);
    }


}
