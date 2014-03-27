package it.capabilities.jira;


import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.RemotePluginUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteDialog;
import com.atlassian.plugin.connect.test.pageobjects.RemoteInlineDialog;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import it.servlet.condition.CheckUsernameConditionServlet;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static it.TestConstants.BARNEY_USERNAME;
import static it.TestConstants.BETTY_USERNAME;
import static it.capabilities.ConnectAsserts.verifyStandardAddOnRelativeQueryParameters;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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
    private static final String ADDON_WEBITEM_DIALOG = "ac-general-web-item-dialog";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), RemotePluginUtils.randomPluginKey())
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
                                .withContext(AddOnUrlContext.addon)
                                .withUrl("http://www.wikipedia.org")
                                .withTarget(
                                        newWebItemTargetBean().withType(WebItemTargetType.inlineDialog)
                                                .withOption("onHover", "true")
                                                .withOption("height", "200px")
                                                .withOption("width", "301px")
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
                                                .withOption("onHover", "true")
                                                .withOption("width", "321px")
                                                .withOption("height", "201px")
                                                .build()
                                )
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("Webitem Dialog Target", "ac.widt"))
                                .withKey(ADDON_WEBITEM_DIALOG)
                                .withLocation("system.top.navigation.bar")
                                .withWeight(1)
                                .withContext(AddOnUrlContext.addon)
                                .withUrl("/my-webitem-dialog")
                                .withTarget(
                                        newWebItemTargetBean().withType(WebItemTargetType.dialog)
                                                .withOption("width", "300px")
                                                .withOption("height", "200px")
                                                .build()
                                )
                                .build()
                )
                .addRoute("/onlyBarneyCondition", new CheckUsernameConditionServlet(BARNEY_USERNAME))
                .addRoute("/onlyBettyCondition", new CheckUsernameConditionServlet(BETTY_USERNAME))
                .addRoute("/irwi?issue_id={issue.id}&project_key={project.key}&pid={project.id}", ConnectAppServlets.helloWorldServlet())
                .addRoute("/my-webitem-dialog", ConnectAppServlets.apRequestServlet())
                .addRoute("/my-webitem-inlinedialog", ConnectAppServlets.apRequestServlet())
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
    public void testAbsoluteWebItem()
    {
        loginAs(BETTY_USERNAME, BETTY_USERNAME);

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ABSOLUTE_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        assertTrue("Web item link should be absolute", webItem.isPointingToACInternalUrl());
        assertThat(webItem.getPath(), startsWith("http://www.google.com/?"));
        assertThat(webItem.getFromQueryString("myProjectKey"), equalTo(project.getKey()));
    }
    
    @Test
    public void testRelativePageWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(PAGE_CONTEXT_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        assertEquals(project.getKey(), webItem.getFromQueryString("project.key"));
        assertEquals(project.getId(), webItem.getFromQueryString("project.id"));
        assertThat(webItem.getPath(), startsWith(product.getProductInstance().getBaseUrl()));
    }

    @Test
    public void testAddonDirectWebItem() throws Exception
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADDON_DIRECT_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        assertEquals(project.getKey(), webItem.getFromQueryString("project_key"));
        assertEquals(project.getId(), webItem.getFromQueryString("pid"));
        assertThat(webItem.getPath(), startsWith(remotePlugin.getAddon().getBaseUrl()));
        verifyStandardAddOnRelativeQueryParameters(webItem, "/jira");
    }

    @Test
    public void testProductWebItem() throws MalformedURLException
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
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
        loginAs(BETTY_USERNAME, BETTY_USERNAME);

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ABSOLUTE_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(ABSOLUTE_WEBITEM)));
    }

    //TODO: once generalPage is complete, add a test to check that a web item pointing to the page works properly

    @Test
    public void testAbsoluteWebItemInlineDialog() throws Exception
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
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
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
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
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADDON_WEBITEM_INLINE_DIALOG), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
        assertTrue("web item should be an inline dialog", webItem.isInlineDialog());
        webItem.click();
        RemoteInlineDialog inlineDialogPage = product.getPageBinder().bind(RemoteInlineDialog.class).waitUntilContentElementNotEmpty("client-http-status");
        assertEquals("Success", inlineDialogPage.getIFrameElementText("message"));
        assertEquals("200", inlineDialogPage.getIFrameElementText("client-http-status"));

    }

    @Test
    public void testAbsoluteWebItemInlineDialogTargetOptions() throws Exception
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADDON_WEBITEM_INLINE_DIALOG), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
        assertTrue("web item should be an inline dialog", webItem.isInlineDialog());
        webItem.hover();
        assertTrue("web item inline dialog should be open", webItem.isActiveInlineDialog());

    }

    @Test
    public void testAbsoluteWebItemDialog() throws Exception
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADDON_WEBITEM_DIALOG), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
        assertTrue("web item should be a dialog", webItem.isDialog());

        // make sure the dialog=1 flag is included and not appended after the jwt.
        // note: if we really want to prove that we've correctly handled the adding of the dialog flag we really need to calculate the canonical url
        URL url = new URL(webItem.getPath());
        String query = url.getQuery();
        int dialogIndex = query.indexOf("dialog=1");
        int jwtIndex = query.indexOf("jwt=");
        assertThat(dialogIndex, is(greaterThanOrEqualTo(0)));
        assertThat(jwtIndex, is(greaterThanOrEqualTo(0)));
        assertThat(jwtIndex, is(greaterThan(dialogIndex))); // must be before the jwt

        webItem.click();
        assertTrue("web item dialog should be open", webItem.isActiveDialog());

        verifyStandardAddOnRelativeQueryParameters(webItem, "/jira");
    }

    @Test
    public void testAbsoluteWebItemDialogDimensions() throws Exception
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADDON_WEBITEM_DIALOG), Optional.<String>absent());
        webItem.click();
        RemoteDialog dialogPage = product.getPageBinder().bind(RemoteDialog.class);
        assertNotEquals("webitem dialog has a height that is not 0", dialogPage.getIFrameSize().getHeight(), 0);
        assertNotEquals("webitem dialog has a width that is not 0", dialogPage.getIFrameSize().getWidth(), 0);

    }

    @Test
    public void testAbsoluteWebItemDialogXdm() throws Exception
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADDON_WEBITEM_DIALOG), Optional.<String>absent());
        webItem.click();
        RemoteDialog dialogPage = product.getPageBinder().bind(RemoteDialog.class).waitUntilContentElementNotEmpty("client-http-status");
        assertEquals("Success", dialogPage.getIFrameElementText("message"));
        assertEquals("200", dialogPage.getIFrameElementText("client-http-status"));

    }

    @Test
    public void testAbsoluteWebItemDialogTargetOptions() throws Exception
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADDON_WEBITEM_DIALOG), Optional.<String>absent());
        webItem.click();
        RemoteDialog dialogPage = product.getPageBinder().bind(RemoteDialog.class);

        assertEquals(dialogPage.getIFrameSize().getHeight(), 200);
        assertEquals(dialogPage.getIFrameSize().getWidth(), 300);

    }
    
    private String getModuleKey(String module)
    {
        return remotePlugin.getAddon().getKey() + ModuleKeyUtils.ADDON_MODULE_SEPARATOR + module;
    }

}
