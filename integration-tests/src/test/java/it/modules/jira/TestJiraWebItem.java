package it.modules.jira;


import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.builder.nested.dialog.DialogOptionsBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.InlineDialogOptions;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteDialog;
import com.atlassian.plugin.connect.test.pageobjects.RemoteInlineDialog;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.CheckUsernameConditionServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions.newDialogOptions;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.modules.ConnectAsserts.verifyStandardAddOnRelativeQueryParameters;
import static it.util.TestUser.ADMIN;
import static it.util.TestUser.BARNEY;
import static it.util.TestUser.BETTY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since 1.0
 */
public class TestJiraWebItem extends JiraWebDriverTestBase
{
    private static final String GENERAL_PAGE = "ac-general-page";
    private static final String PAGE_CONTEXT_WEBITEM = "ac-general-web-item";
    private static final String ADDON_DIRECT_WEBITEM = "ac-direct-to-addon-web-item";
    private static final String PRODUCT_WEBITEM = "quick-project-link";
    private static final String ABSOLUTE_WEBITEM = "google-link";
    private static final String ABSOLUTE_WEBITEM_INLINE_DIALOG = "wikipedia-link";
    private static final String ADDON_WEBITEM_INLINE_DIALOG = "ac-general-web-item-inline-dialog";
    private static final String NULL_CHROME_VARIANT = "NullChrome";
    private static final String CHROMELESS_VARIANT = "Chromeless";
    private static final String ADDON_WEBITEM_DIALOG = "ac-general-web-item-dialog";
    private static final String ABSOLUTE_WEBITEM_DIALOG = "ac-general-web-item-dialog-absolute";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addScope(ScopeName.READ)
                .addInstallLifecycle()
                .addRoute(ConnectRunner.INSTALLED_PATH, ConnectAppServlets.helloWorldServlet())
                .addModule("generalPages",
                        newPageBean()
                            .withName(new I18nProperty("A General Page", null))
                            .withKey(GENERAL_PAGE)
                            .withLocation("not a real location so no web item is displayed")
                            .withUrl("/irwi?issue_id={issue.id}&project_key={project.key}&pid={project.id}")
                            .build())
                .addModules("webItems",
                        newWebItemBean()
                                .withContext(AddOnUrlContext.page)
                                .withName(new I18nProperty("AC General Web Item", "ac.gen"))
                                .withKey(PAGE_CONTEXT_WEBITEM)
                                .withLocation("system.top.navigation.bar")
                                .withWeight(5)
                                .withUrl(GENERAL_PAGE)
                                .build(),
                        newWebItemBean()
                                .withContext(AddOnUrlContext.addon)
                                .withName(new I18nProperty("AC Direct To Addon Web Item", "ac.dir"))
                                .withKey(ADDON_DIRECT_WEBITEM)
                                .withLocation("system.top.navigation.bar")
                                .withWeight(4)
                                .withUrl("/irwi?issue_id={issue.id}&project_key={project.key}&pid={project.id}")
                                .build(),
                        newWebItemBean()
                                .withContext(AddOnUrlContext.product)
                                .withName(new I18nProperty("Quick project link", "ac.qp"))
                                .withKey(PRODUCT_WEBITEM)
                                .withLocation("system.top.navigation.bar")
                                .withWeight(3)
                                .withUrl("/browse/ACDEV-1234?project_key={project.key}")
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("google link", "ac.gl"))
                                .withKey(ABSOLUTE_WEBITEM)
                                .withLocation("system.top.navigation.bar")
                                .withWeight(2)
                                .withUrl("http://www.google.com?myProjectKey={project.key}")
                                .withConditions(
                                        newSingleConditionBean().withCondition("user_is_logged_in").build()
                                        , newSingleConditionBean().withCondition("/onlyBettyCondition").build()
                                )
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("wikipedia link", "ac.ild"))
                                .withKey(ABSOLUTE_WEBITEM_INLINE_DIALOG)
                                .withLocation("system.top.navigation.bar")
                                .withWeight(1)
                                .withUrl("http://www.wikipedia.org")
                                .withTarget(
                                        newWebItemTargetBean().withType(WebItemTargetType.inlineDialog)
                                                .withOptions(InlineDialogOptions.newInlineDialogOptions()
                                                                .withOnHover(true)
                                                                .withWidth("301px")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("Webitem inlineDialog Target", "ac.awidt"))
                                .withKey(ADDON_WEBITEM_INLINE_DIALOG)
                                .withLocation("system.top.navigation.bar")
                                .withWeight(1)
                                .withContext(AddOnUrlContext.addon)
                                .withUrl("/my-webitem-inlinedialog")
                                .withTarget(
                                        newWebItemTargetBean().withType(WebItemTargetType.inlineDialog)
                                                .withOptions(InlineDialogOptions.newInlineDialogOptions()
                                                                .withOnHover(true)
                                                                .withWidth("321px")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build(),
                        webItemWithDialogOptions(true),
                        webItemWithDialogOptions(false),
                        webItemWithDialogOptions(null),
                        absoluteWebItemWithDialogOptions(true),
                        absoluteWebItemWithDialogOptions(false),
                        absoluteWebItemWithDialogOptions(null)
                )
                .addRoute("/onlyBarneyCondition", new CheckUsernameConditionServlet(BARNEY.getUsername()))
                .addRoute("/onlyBettyCondition", new CheckUsernameConditionServlet(BETTY.getUsername()))
                .addRoute("/irwi?issue_id={issue.id}&project_key={project.key}&pid={project.id}", ConnectAppServlets.helloWorldServlet())
                .addRoute("/my-webitem-dialog", ConnectAppServlets.apRequestServlet())
                .addRoute("/my-webitem-dialog" + CHROMELESS_VARIANT, ConnectAppServlets.apRequestServlet())
                .addRoute("/my-webitem-dialog" + NULL_CHROME_VARIANT, ConnectAppServlets.apRequestServlet())
                .addRoute("/my-webitem-inlinedialog", ConnectAppServlets.apRequestServlet())
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
    public void testAbsoluteWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(BETTY, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ABSOLUTE_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        assertThat(webItem.getPath(), startsWith("http://www.google.com/?"));
        assertThat(webItem.getFromQueryString("myProjectKey"), equalTo(project.getKey()));
    }

    @Test
    public void testRelativePageWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(ADMIN, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(PAGE_CONTEXT_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        assertEquals(project.getKey(), webItem.getFromQueryString("project.key"));
        assertEquals(project.getId(), webItem.getFromQueryString("project.id"));
        assertThat(webItem.getPath(), startsWith(product.getProductInstance().getBaseUrl()));
    }

    @Test
    public void testAddonDirectWebItem() throws Exception
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(ADMIN, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADDON_DIRECT_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        assertEquals(project.getKey(), webItem.getFromQueryString("project_key"));
        assertEquals(project.getId(), webItem.getFromQueryString("pid"));
        assertThat(webItem.getPath(), startsWith(runner.getAddon().getBaseUrl()));
        verifyStandardAddOnRelativeQueryParameters(webItem, "/jira");
    }

    @Test
    public void testProductWebItem() throws MalformedURLException
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(ADMIN, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(PRODUCT_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        webItem.click();

        URL url = new URL(webItem.getPath());
        assertThat(url.getPath(), is("/jira/browse/ACDEV-1234"));
        assertEquals(project.getKey(), webItem.getFromQueryString("project_key"));
    }

    @Test
    public void bettyCanSeeWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(BETTY, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ABSOLUTE_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(ADMIN, JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(ABSOLUTE_WEBITEM)));
    }

    //TODO: once generalPage is complete, add a test to check that a web item pointing to the page works properly

    @Test
    public void testAbsoluteWebItemInlineDialog() throws Exception
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(ADMIN, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ABSOLUTE_WEBITEM_INLINE_DIALOG), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
        assertTrue("web item should be an inline dialog", webItem.isInlineDialog());
        webItem.click();
        RemoteInlineDialog inlineDialogPage = product.getPageBinder().bind(RemoteInlineDialog.class);
        assertNotNull("web item inline dialog should be open", inlineDialogPage);
    }

    @Test
    public void testAddonWebItemInlineDialog() throws Exception
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(ADMIN, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADDON_WEBITEM_INLINE_DIALOG), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
        assertTrue("web item should be an inline dialog", webItem.isInlineDialog());
        webItem.click();
        RemoteInlineDialog inlineDialogPage = product.getPageBinder().bind(RemoteInlineDialog.class);
        assertNotNull("web item inline dialog should be open", inlineDialogPage);
    }

    @Test
    public void testAbsoluteWebItemInlineDialogXdm() throws Exception
    {
        testWebItemInlineDialogXdm(ABSOLUTE_WEBITEM_INLINE_DIALOG);
    }

    @Test
    public void testAddOnWebItemInlineDialogXdm() throws Exception
    {
        RemoteInlineDialog inlineDialogPage = testWebItemInlineDialogXdm(ADDON_WEBITEM_INLINE_DIALOG);
        assertEquals("Success", inlineDialogPage.getIFrameElementText("message"));
        assertEquals("200", inlineDialogPage.getIFrameElementText("client-http-status"));
    }

    private RemoteInlineDialog testWebItemInlineDialogXdm(String moduleKey) throws Exception
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(ADMIN, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(moduleKey), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
        assertTrue("web item should be an inline dialog", webItem.isInlineDialog());
        webItem.click();
        return product.getPageBinder().bind(RemoteInlineDialog.class).waitUntilContentElementNotEmpty("client-http-status");
    }

    @Test
    public void testAbsoluteWebItemInlineDialogTargetOptions() throws Exception
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(ADMIN, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADDON_WEBITEM_INLINE_DIALOG), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
        assertTrue("web item should be an inline dialog", webItem.isInlineDialog());
        webItem.hover();
        assertTrue("web item inline dialog should be open", webItem.isActiveInlineDialog());

    }

    @Test
    public void testAbsoluteWebItemDialog() throws Exception
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(ADMIN, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ABSOLUTE_WEBITEM_DIALOG), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
        assertTrue("web item should be a dialog", webItem.isDialog());
        assertThat("absolute web item link should start with 'http'", webItem.getPath(), startsWith("http"));

        URL url = new URL(webItem.getPath());
        String query = url.getQuery();
        int dialogIndex = query.indexOf("dialog=1");
        int jwtIndex = query.indexOf("jwt=");
        assertThat(dialogIndex, is(-1)); // not applicable for absolute links
        assertThat(jwtIndex, is(-1)); // not applicable for absolute links

        webItem.click();
        assertTrue("web item dialog should be open", webItem.isActiveDialog());
    }

    @Test
    public void testAddOnWebItemDialog() throws Exception
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(ADMIN, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADDON_WEBITEM_DIALOG), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
        assertTrue("web item should be a dialog", webItem.isDialog());

        URL url = new URL(webItem.getPath());
        String query = url.getQuery();
        int dialogIndex = query.indexOf("dialog=1");
        int jwtIndex = query.indexOf("jwt=");
        assertThat(dialogIndex, is(-1)); // added by the iframe servlet
        assertThat(jwtIndex, is(-1)); // added by the iframe servlet

        webItem.click();
        assertTrue("web item dialog should be open", webItem.isActiveDialog());
    }

    @Test
    public void testAddOnWebItemDialogDimensions() throws Exception
    {
        testWebItemDialogDimensions(ADDON_WEBITEM_DIALOG);
    }

    @Test
    public void testAbsoluteWebItemDialogDimensions() throws Exception
    {
        testWebItemDialogDimensions(ABSOLUTE_WEBITEM_DIALOG);
    }

    private void testWebItemDialogDimensions(String moduleKey) throws Exception
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(ADMIN, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(moduleKey), Optional.<String>absent());
        webItem.click();
        RemoteDialog dialogPage = product.getPageBinder().bind(RemoteDialog.class);
        assertThat("webitem dialog has a height that is not 0", dialogPage.getIFrameSize().getHeight(), is(not(0)));
        assertThat("webitem dialog has a width that is not 0", dialogPage.getIFrameSize().getWidth(), is(not(0)));

    }

    @Test
    public void testAddOnWebItemDialogXdm() throws Exception
    {
        testWebItemDialogXdm(ADDON_WEBITEM_DIALOG);
    }

    @Test
    public void testAbsoluteWebItemDialogXdm() throws Exception
    {
        testWebItemDialogXdm(ABSOLUTE_WEBITEM_DIALOG);
    }

    private void testWebItemDialogXdm(String moduleKey) throws Exception
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(ADMIN, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(moduleKey), Optional.<String>absent());
        webItem.click();
        RemoteDialog dialogPage = product.getPageBinder().bind(RemoteDialog.class).waitUntilContentElementNotEmpty("client-http-status");
        assertEquals("Success", dialogPage.getIFrameElementText("message"));
        assertEquals("200", dialogPage.getIFrameElementText("client-http-status"));

    }

    @Test
    public void testAddOnWebItemDialogTargetOptions() throws Exception
    {
        testWebItemDialogTargetOptions(true, ADDON_WEBITEM_DIALOG);
    }

    @Test
    public void testAddOnWebItemDialogTargetOptionsChromeless() throws Exception
    {
        testWebItemDialogTargetOptions(false, ADDON_WEBITEM_DIALOG);
    }

    @Test
    public void testAddOnWebItemDialogTargetOptionsNullChrome() throws Exception
    {
        testWebItemDialogTargetOptions(null, ADDON_WEBITEM_DIALOG);
    }

    @Test
    public void testAbsoluteWebItemDialogTargetOptions() throws Exception
    {
        testWebItemDialogTargetOptions(true, ABSOLUTE_WEBITEM_DIALOG);
    }

    @Test
    public void testAbsoluteWebItemDialogTargetOptionsChromeless() throws Exception
    {
        testWebItemDialogTargetOptions(false, ABSOLUTE_WEBITEM_DIALOG);
    }

    @Test
    public void testAbsoluteWebItemDialogTargetOptionsNullChrome() throws Exception
    {
        testWebItemDialogTargetOptions(null, ABSOLUTE_WEBITEM_DIALOG);
    }

    private void testWebItemDialogTargetOptions(Boolean chrome, String moduleKey) throws Exception
    {
        String dialogOptionKey = dialogOptionKey(chrome, moduleKey);

        JiraViewProjectPage viewProjectPage = loginAndVisit(ADMIN, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(dialogOptionKey), Optional.<String>absent());
        webItem.click();
        RemoteDialog dialogPage = product.getPageBinder().bind(RemoteDialog.class);

        assertEquals(dialogPage.getIFrameSize().getHeight(), 200);
        assertEquals(dialogPage.getIFrameSize().getWidth(), 300);

        // js code defaults to true
        boolean isChrome = chrome == null || chrome;
        assertEquals(isChrome, dialogPage.hasChrome());
    }

    private String getModuleKey(String module)
    {
        return addonAndModuleKey(runner.getAddon().getKey(), module);
    }

    private static WebItemModuleBean webItemWithDialogOptions(Boolean chrome)
    {
        return webItemWithDialogOptions(chrome, ADDON_WEBITEM_DIALOG);
    }

    private static WebItemModuleBean absoluteWebItemWithDialogOptions(Boolean chrome)
    {
        return webItemWithDialogOptions(chrome, ABSOLUTE_WEBITEM_DIALOG);
    }

    private static WebItemModuleBean webItemWithDialogOptions(Boolean chrome, String moduleKeyPrefix)
    {
        String variant = dialogOptionVariant(chrome);

        DialogOptionsBuilder dialogOptionsBuilder = newDialogOptions()
                .withWidth("300px")
                .withHeight("200px");

        if (chrome != null)
        {
            dialogOptionsBuilder.withChrome(chrome);
        }

        return newWebItemBean()
                .withName(new I18nProperty("Webitem Dialog Target " + variant, null))
                .withKey(moduleKeyPrefix + variant)
                .withLocation("system.top.navigation.bar")
                .withWeight(1)
                .withContext(AddOnUrlContext.addon)
                .withUrl("/my-webitem-dialog" + variant)
                .withTarget(
                        newWebItemTargetBean().withType(WebItemTargetType.dialog)
                                .withOptions(dialogOptionsBuilder.build())
                                .build()
                )
                .build();
    }


    private static String dialogOptionKey(Boolean chrome, String moduleKey)
    {
        return moduleKey + dialogOptionVariant(chrome);
    }

    private static String dialogOptionVariant(Boolean chrome)
    {
        String variant = "";
        if (chrome == null)
        {
            variant = NULL_CHROME_VARIANT;
        }
        else if (!chrome)
        {
            variant = CHROMELESS_VARIANT;
        }
        return variant;
    }

}
