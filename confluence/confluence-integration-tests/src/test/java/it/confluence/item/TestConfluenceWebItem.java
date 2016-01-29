package it.confluence.item;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import com.atlassian.connect.test.confluence.pageobjects.ConfluenceOps.ConfluencePageData;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceViewPage;
import com.atlassian.fugue.Pair;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.AddonUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonHelloWorldPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.condition.CheckUsernameConditionServlet;
import com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.TestUser;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import it.confluence.ConfluenceWebDriverTestBase;
import redstone.xmlrpc.XmlRpcFault;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.test.common.matcher.ConnectAsserts
        .verifyContainsStandardAddonQueryParameters;
import static com.atlassian.plugin.connect.test.common.matcher.IsInteger.isInteger;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    private static final String ADDON_WEBITEM_INLINE_DIALOG = "wikipedia-link";
    private static final String SPACE = "ds";

    private static final ParameterCapturingServlet PARAMETER_CAPTURING_DIRECT_WEBITEM_SERVLET = ConnectAppServlets.parameterCapturingPageServlet();

    private static ConnectRunner remotePlugin;

    private static TestUser betty;
    private static TestUser barney;

    @BeforeClass
    public static void startConnectAddon() throws Exception
    {
        barney = testUserFactory.basicUser();
        betty = testUserFactory.admin();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .setAuthenticationToNone()
                .addModules("webItems",
                        newWebItemBean()
                                .withName(new I18nProperty("AC General Web Item", null))
                                .withKey(ADDON_WEBITEM)
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("/irwi?page_id={page.id}&content_id={content.id}")
                                .build(),
                        newWebItemBean()
                                .withContext(AddonUrlContext.addon)
                                .withName(new I18nProperty("AC Direct To Addon Web Item", null))
                                .withKey(ADDON_DIRECT_WEBITEM)
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("/irwi?page_id={page.id}&content_id={content.id}")
                                .build(),
                        newWebItemBean()
                                .withContext(AddonUrlContext.product)
                                .withName(new I18nProperty("Quick page link", null))
                                .withKey(PRODUCT_WEBITEM)
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("/pages/viewpage.action?pageId={page.id}&contentId={content.id}")
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("google link", null))
                                .withKey(ABSOLUTE_WEBITEM)
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("http://www.google.com?myPageId={page.id}&mySpaceKey={space.key}&myContentId={content.id}")
                                .withConditions(
                                        newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                        newSingleConditionBean().withCondition("/only" + betty.getDisplayName() + "Condition").build()
                                ).build(),
                        newWebItemBean()
                                .withName(new I18nProperty("wikipedia link", null))
                                .withKey(ADDON_WEBITEM_INLINE_DIALOG)
                                .withLocation("system.content.metadata")
                                .withWeight(1)
                                .withContext(AddonUrlContext.addon)
                                .withUrl("http://www.wikipedia.org")
                                .withTarget(
                                        newWebItemTargetBean().withType(WebItemTargetType.inlineDialog).build()
                                )
                                .build())
                .addRoute("/only" + barney.getDisplayName() + "Condition", new CheckUsernameConditionServlet(barney))
                .addRoute("/only" + betty.getDisplayName() + "Condition", new CheckUsernameConditionServlet(betty))
                .addRoute("/irwi", ConnectAppServlets.wrapContextAwareServlet(PARAMETER_CAPTURING_DIRECT_WEBITEM_SERVLET))
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void testAbsoluteWebItem() throws Exception
    {
        login(betty);

        RemoteWebItem webItem = findViewPageWebItem(getModuleKey(ABSOLUTE_WEBITEM)).right();
        assertNotNull("Web item should be found", webItem);

        assertThat(webItem.getPath(), startsWith("http://www.google.com/?"));
        assertThat(webItem.getFromQueryString("myPageId"), isInteger());
        assertThat(webItem.getFromQueryString("myContentId"), equalTo(webItem.getFromQueryString("myPageId")));
        assertThat(webItem.getFromQueryString("mySpaceKey"), equalTo("ds"));
    }

    @Test
    public void testRelativeWebItem() throws Exception
    {
        testAddonWebItem(ADDON_WEBITEM);
    }

    @Test
    public void testAddonDirectWebItem() throws Exception
    {
        testAddonWebItem(ADDON_DIRECT_WEBITEM);
    }

    private void testAddonWebItem(final String addonDirectWebitem) throws Exception
    {
        login(testUserFactory.admin());

        Pair<ConfluenceViewPage, RemoteWebItem> pageAndWebItem = findViewPageWebItem(getModuleKey(addonDirectWebitem));
        String pageId = pageAndWebItem.left().getPageId();

        RemoteWebItem webItem = pageAndWebItem.right();
        assertNotNull("Web item should be found", webItem);

        webItem.click();
        product.getPageBinder().bind(ConnectAddonHelloWorldPage.class);
        Map<String, String> queryParams = PARAMETER_CAPTURING_DIRECT_WEBITEM_SERVLET.getParamsFromLastRequest();

        assertEquals(pageId, queryParams.get("page_id"));
        assertEquals(pageId, queryParams.get("content_id"));
        verifyContainsStandardAddonQueryParameters(queryParams, "/confluence");
    }

    @Test
    public void testProductWebItem() throws Exception
    {
        login(testUserFactory.admin());

        ConfluenceViewPage viewPage = createAndVisitViewPage();

        RemoteWebItem webItem = confluencePageOperations.findWebItem(getModuleKey(PRODUCT_WEBITEM), Optional.of("action-menu-link"));
        assertNotNull("Web item should be found", webItem);

        webItem.click();

        URL url = new URL(webItem.getPath());
        assertThat(url.getPath(), is("/confluence/pages/viewpage.action"));
        assertEquals(viewPage.getPageId(), webItem.getFromQueryString("pageId"));
        assertEquals(viewPage.getPageId(), webItem.getFromQueryString("contentId"));
    }

    @Test
    public void bettyCanSeeWebItem() throws Exception
    {
        login(betty);

        RemoteWebItem webItem = findViewPageWebItem(getModuleKey(ABSOLUTE_WEBITEM)).right();

        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyWebItem() throws Exception
    {
        login(testUserFactory.admin());
        createAndVisitViewPage();
        assertFalse("Web item should NOT be found", confluencePageOperations.existsWebItem(getModuleKey(ABSOLUTE_WEBITEM)));
    }


    @Test
    public void testAddonWebItemInlineDialog() throws Exception
    {
        login(testUserFactory.admin());

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
        return Pair.pair(viewPage, confluencePageOperations.findWebItem(webItemId, Optional.<String>empty()));
    }

    private ConfluenceViewPage createAndVisitViewPage() throws Exception
    {
        return createAndVisitPage(ConfluenceViewPage.class);
    }


    private <P extends Page> P createAndVisitPage(Class<P> pageClass) throws Exception
    {
        ConfluencePageData pageData = createPage();
        return product.visit(pageClass, pageData.getId());
    }

    private ConfluencePageData createPage() throws MalformedURLException, XmlRpcFault
    {
        return confluenceOps.setPage(some(testUserFactory.admin()), SPACE, "Page with webitem", "some page content");
    }


    private String getModuleKey(String module)
    {
        return addonAndModuleKey(remotePlugin.getAddon().getKey(), module);
    }


}
