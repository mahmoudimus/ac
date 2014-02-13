package it.capabilities.jira;


import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.pageobjects.RemoteDialog;
import com.atlassian.plugin.connect.test.pageobjects.RemoteInlineDialog;
import com.atlassian.plugin.connect.test.pageobjects.RemotePageUtil;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import it.capabilities.CheckUsernameConditionServlet;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static it.TestConstants.BARNEY_USERNAME;
import static it.TestConstants.BETTY_USERNAME;
import static it.capabilities.ConnectAsserts.assertURIEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @since 1.0
 */
public class TestJiraWebItem extends JiraWebDriverTestBase
{
    private static final String ADDON_WEBITEM = "ac-general-web-item";
    private static final String ADDON_DIRECT_WEBITEM = "ac-direct-to-addon-web-item";
    private static final String PRODUCT_WEBITEM = "quick-project-link";
    private static final String ABSOLUTE_WEBITEM = "google-link";
    private static final String ADDON_WEBITEM_INLINE_DIALOG = "ac-general-web-item-inline-dialog";
    private static final String ADDON_WEBITEM_DIALOG = "ac-general-web-item-dialog";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addScope(ScopeName.READ)
                .addInstallLifecycle()
                .addRoute(ConnectRunner.INSTALLED_PATH, ConnectAppServlets.helloWorldServlet())
                .addModules("webItems",
                        newWebItemBean()
                                .withContext(AddOnUrlContext.page)
                                .withName(new I18nProperty("AC General Web Item", "ac.gen"))
                                .withKey("ac-general-web-item")
                                .withLocation("system.top.navigation.bar")
                                .withWeight(1)
                                .withUrl("/irwi?issue_id={issue.id}&project_key={project.key}&pid={project.id}")
                                .build(),
                        newWebItemBean()
                                .withContext(AddOnUrlContext.addon)
                                .withName(new I18nProperty("AC Direct To Addon Web Item", "ac.dir"))
                                .withKey("ac-direct-to-addon-web-item")
                                .withLocation("system.top.navigation.bar")
                                .withWeight(1)
                                .withUrl("/irwi?issue_id={issue.id}&project_key={project.key}&pid={project.id}")
                                .build(),
                        newWebItemBean()
                                .withContext(AddOnUrlContext.product)
                                .withName(new I18nProperty("Quick project link", "ac.qp"))
                                .withKey("quick-project-link")
                                .withLocation("system.top.navigation.bar")
                                .withWeight(1)
                                .withUrl("/browse/ACDEV-1234?project_key={project.key}")
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("google link", "ac.gl"))
                                .withKey("google-link")
                                .withLocation("system.top.navigation.bar")
                                .withWeight(1)
                                .withUrl("http://www.google.com")
                                .withConditions(
                                        newSingleConditionBean().withCondition("user_is_logged_in").build()
                                        , newSingleConditionBean().withCondition("/onlyBettyCondition").build()
                                )
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("Webitem inlineDialog Target", "ac.ild"))
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
                                .withName(new I18nProperty("Webitem Dialog Target", null))
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
        RemoteWebItem webItem = viewProjectPage.findWebItem(ABSOLUTE_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        assertTrue("Web item link should be absolute", webItem.isPointingToACInternalUrl());
        assertURIEquals("http://www.google.com", webItem.getPath());
    }
    
    @Test
    public void testRelativeWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(ADDON_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        assertEquals(project.getKey(), webItem.getFromQueryString("project_key"));
        assertEquals(project.getId(), webItem.getFromQueryString("pid"));
        assertThat(webItem.getPath(), startsWith(product.getProductInstance().getBaseUrl()));
    }

    @Test
    public void testAddonDirectWebItem() throws Exception
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(ADDON_DIRECT_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        assertEquals(project.getKey(), webItem.getFromQueryString("project_key"));
        assertEquals(project.getId(), webItem.getFromQueryString("pid"));
        assertThat(webItem.getPath(), startsWith(remotePlugin.getAddon().getBaseUrl()));
    }

    @Test
    public void testProductWebItem() throws MalformedURLException
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(PRODUCT_WEBITEM, Optional.<String>absent());
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
        RemoteWebItem webItem = viewProjectPage.findWebItem(ABSOLUTE_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(ABSOLUTE_WEBITEM));
    }

    //TODO: once generalPage is complete, add a test to check that a web item pointing to the page works properly

    @Test
    public void testAbsoluteWebItemInlineDialog() throws Exception
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(ADDON_WEBITEM_INLINE_DIALOG, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
        assertTrue("web item should be an inline dialog", webItem.isInlineDialog());
        webItem.click();
        product.getPageBinder().bind(RemoteInlineDialog.class).waitUntilContentElementNotEmpty("client-http-status");
        assertTrue("web item inline dialog should be open", webItem.isActiveInlineDialog());
    }

    @Test
    public void testAbsoluteWebItemInlineDialogXdm() throws Exception
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(ADDON_WEBITEM_INLINE_DIALOG, Optional.<String>absent());
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
        RemoteWebItem webItem = viewProjectPage.findWebItem(ADDON_WEBITEM_INLINE_DIALOG, Optional.<String>absent());
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
        RemoteWebItem webItem = viewProjectPage.findWebItem(ADDON_WEBITEM_DIALOG, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
        assertTrue("web item should be a dialog", webItem.isDialog());
        webItem.click();
        assertTrue("web item dialog should be open", webItem.isActiveDialog());
    }

    @Test
    public void testAbsoluteWebItemDialogXdm() throws Exception
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(ADDON_WEBITEM_DIALOG, Optional.<String>absent());
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
        RemoteWebItem webItem = viewProjectPage.findWebItem(ADDON_WEBITEM_DIALOG, Optional.<String>absent());
        webItem.click();
        RemoteDialog dialogPage = product.getPageBinder().bind(RemoteDialog.class);

        assertEquals(dialogPage.getIFrameSize().getHeight(), 200);
        assertEquals(dialogPage.getIFrameSize().getWidth(), 300);

    }

}
