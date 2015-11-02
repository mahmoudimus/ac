package it.confluence.item;

import java.net.MalformedURLException;
import java.net.URL;

import com.atlassian.connect.test.confluence.pageobjects.ConfluenceOps;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceViewPage;
import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.matcher.ConnectAsserts;
import com.atlassian.plugin.connect.test.common.matcher.IsInteger;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.condition.CheckUsernameConditionServlet;
import com.atlassian.plugin.connect.test.common.util.TestUser;

import com.google.common.base.Optional;

import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import it.confluence.ConfluenceWebDriverTestBase;
import redstone.xmlrpc.XmlRpcFault;


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

    private static TestUser betty;
    private static TestUser barney;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        barney = testUserFactory.basicUser();
        betty = testUserFactory.admin();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("webItems",
                        WebItemModuleBean.newWebItemBean()
                                .withName(new I18nProperty("AC General Web Item", null))
                                .withKey(ADDON_WEBITEM)
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("/irwi?page_id={page.id}&content_id={content.id}")
                                .build(),
                        WebItemModuleBean.newWebItemBean()
                                .withContext(AddOnUrlContext.addon)
                                .withName(new I18nProperty("AC Direct To Addon Web Item", null))
                                .withKey(ADDON_DIRECT_WEBITEM)
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("/irwi?page_id={page.id}&content_id={content.id}")
                                .build(),
                        WebItemModuleBean.newWebItemBean()
                                .withContext(AddOnUrlContext.product)
                                .withName(new I18nProperty("Quick page link", null))
                                .withKey(PRODUCT_WEBITEM)
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("/pages/viewpage.action?pageId={page.id}&contentId={content.id}")
                                .build(),
                        WebItemModuleBean.newWebItemBean()
                                .withName(new I18nProperty("google link", null))
                                .withKey(ABSOLUTE_WEBITEM)
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withUrl("http://www.google.com?myPageId={page.id}&mySpaceKey={space.key}&myContentId={content.id}")
                                .withConditions(
                                        SingleConditionBean.newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                        SingleConditionBean.newSingleConditionBean().withCondition("/only" + betty.getDisplayName() + "Condition").build()
                                ).build(),
                        WebItemModuleBean.newWebItemBean()
                                .withName(new I18nProperty("wikipedia link", null))
                                .withKey(ADDON_WEBITEM_INLINE_DIALOG)
                                .withLocation("system.content.metadata")
                                .withWeight(1)
                                .withContext(AddOnUrlContext.addon)
                                .withUrl("http://www.wikipedia.org")
                                .withTarget(
                                        WebItemTargetBean.newWebItemTargetBean().withType(WebItemTargetType.inlineDialog).build()
                                )
                                .build())
                .addRoute("/only" + barney.getDisplayName() + "Condition", new CheckUsernameConditionServlet(barney))
                .addRoute("/only" + betty.getDisplayName() + "Condition", new CheckUsernameConditionServlet(betty))
                .addRoute("/irwi?page_id={page.id}&content_id={content.id}", ConnectAppServlets.helloWorldServlet())
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
        login(betty);

        RemoteWebItem webItem = findViewPageWebItem(getModuleKey(ABSOLUTE_WEBITEM)).right();
        Assert.assertNotNull("Web item should be found", webItem);

        Assert.assertThat(webItem.getPath(), CoreMatchers.startsWith("http://www.google.com/?"));
        Assert.assertThat(webItem.getFromQueryString("myPageId"), IsInteger.isInteger());
        Assert.assertThat(webItem.getFromQueryString("myContentId"), CoreMatchers.equalTo(webItem.getFromQueryString("myPageId")));
        Assert.assertThat(webItem.getFromQueryString("mySpaceKey"), CoreMatchers.equalTo("ds"));
    }

    @Test
    public void testRelativeWebItem() throws Exception
    {
        login(testUserFactory.admin());

        Pair<ConfluenceViewPage, RemoteWebItem> pageAndWebItem = findViewPageWebItem(getModuleKey(ADDON_WEBITEM));
        RemoteWebItem webItem = pageAndWebItem.right();
        Assert.assertNotNull("Web item should be found", webItem);

        Assert.assertEquals(pageAndWebItem.left().getPageId(), webItem.getFromQueryString("page_id"));
        Assert.assertEquals(pageAndWebItem.left().getPageId(), webItem.getFromQueryString("content_id"));
        // web-item url mode is relative to the addon by default
        Assert.assertThat(webItem.getPath(), CoreMatchers.startsWith(remotePlugin.getAddon().getBaseUrl()));

        ConnectAsserts.verifyStandardAddOnRelativeQueryParameters(webItem, "/confluence");
    }

    @Test
    public void testAddonDirectWebItem() throws Exception
    {
        login(testUserFactory.admin());

        Pair<ConfluenceViewPage, RemoteWebItem> pageAndWebItem = findViewPageWebItem(getModuleKey(ADDON_DIRECT_WEBITEM));
        RemoteWebItem webItem = pageAndWebItem.right();
        Assert.assertNotNull("Web item should be found", webItem);

        Assert.assertEquals(pageAndWebItem.left().getPageId(), webItem.getFromQueryString("page_id"));
        Assert.assertEquals(pageAndWebItem.left().getPageId(), webItem.getFromQueryString("content_id"));
        Assert.assertThat(webItem.getPath(), CoreMatchers.startsWith(remotePlugin.getAddon().getBaseUrl()));

        ConnectAsserts.verifyStandardAddOnRelativeQueryParameters(webItem, "/confluence");
    }

    @Test
    public void testProductWebItem() throws Exception
    {
        login(testUserFactory.admin());

        ConfluenceViewPage viewPage = createAndVisitViewPage();

        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(PRODUCT_WEBITEM), Optional.of("action-menu-link"));
        Assert.assertNotNull("Web item should be found", webItem);

        webItem.click();

        URL url = new URL(webItem.getPath());
        Assert.assertThat(url.getPath(), CoreMatchers.is("/confluence/pages/viewpage.action"));
        Assert.assertEquals(viewPage.getPageId(), webItem.getFromQueryString("pageId"));
        Assert.assertEquals(viewPage.getPageId(), webItem.getFromQueryString("contentId"));
    }

    @Test
    public void bettyCanSeeWebItem() throws Exception
    {
        login(betty);

        RemoteWebItem webItem = findViewPageWebItem(getModuleKey(ABSOLUTE_WEBITEM)).right();

        Assert.assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyWebItem() throws Exception
    {
        login(testUserFactory.admin());
        createAndVisitViewPage();
        Assert.assertFalse("Web item should NOT be found", connectPageOperations.existsWebItem(getModuleKey(ABSOLUTE_WEBITEM)));
    }


    @Test
    public void testAddonWebItemInlineDialog() throws Exception
    {
        login(testUserFactory.admin());

        Pair<ConfluenceViewPage, RemoteWebItem> pageAndWebItem = findViewPageWebItem(getModuleKey(ADDON_WEBITEM_INLINE_DIALOG));
        RemoteWebItem webItem = pageAndWebItem.right();
        Assert.assertNotNull("Web item should be found", webItem);
        Assert.assertTrue("web item should be an inline dialog", webItem.isInlineDialog());
        webItem.click();
        Assert.assertTrue("web item inline dialog should be open", webItem.isActiveInlineDialog());
    }

    private Pair<ConfluenceViewPage, RemoteWebItem> findViewPageWebItem(String webItemId) throws Exception
    {
        ConfluenceViewPage viewPage = createAndVisitViewPage();
        return Pair.pair(viewPage, connectPageOperations.findWebItem(webItemId, Optional.<String>absent()));
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
        return confluenceOps.setPage(Option.some(testUserFactory.admin()), SPACE, "Page with webitem", "some page content");
    }


    private String getModuleKey(String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(),module);
    }


}
