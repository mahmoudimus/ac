package it.jira;

import com.atlassian.jira.pageobjects.dialogs.ShifterDialog;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.JiraAdminHomePage;
import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailPage;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.pageobjects.jira.PlainTextView;
import com.atlassian.plugin.connect.test.pageobjects.jira.ViewChangingSearchResult;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import hudson.plugins.jira.soap.RemoteIssue;
import it.servlet.ConnectAppServlets;
import org.junit.*;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static it.TestConstants.ADMIN_FULL_NAME;
import static org.junit.Assert.*;

public class TestJira extends JiraWebDriverTestBase
{
    private static final String ADMIN_KEY = "addon-admin";
    private static final String ADVANCED_ADMIN_KEY = "advanced-addon-admin";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("adminPages",
                        newPageBean()
                                .withKey(ADMIN_KEY)
                                .withName(new I18nProperty("Addon Admin", "addon.admin"))
                                .withUrl("/admin")
                                .build(),
                        newPageBean()
                                .withKey(ADVANCED_ADMIN_KEY)
                                .withName(new I18nProperty("Addon Advanced Admin", "addon.admin.advanced"))
                                .withUrl("/advanced-admin")
                                .withLocation("advanced_menu_section/advanced_section")
                                .build())
                .addRoute("/admin", ConnectAppServlets.apRequestServlet())
                .addRoute("/advanced-admin", ConnectAppServlets.apRequestServlet())
                .addModule("jiraIssueTabPanels",
                        newTabPanelBean()
                                .withKey("jira-remotePluginIssueTabPage")
                                .withName(new I18nProperty("AC Play Issue Tab Page", "issue.tab"))
                                .withUrl("/issue-tab-panel")
                                .build())
                .addRoute("/issue-tab-panel", ConnectAppServlets.apRequestServlet())
                .addModule("webItems",
                        newWebItemBean()
                                .withKey("jira-issueAction")
                                .withName(new I18nProperty("Test Issue Action", "issue.action"))
                                .withUrl("/jia")
                                .withLocation("operations-subtasks")
                                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.dialog).build())
                                .build())
                .addRoute("/jia", ConnectAppServlets.dialogServlet())
                .addScope(ScopeName.READ)
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
    @Ignore("partly ported from xml to json: see comments")
    @XmlDescriptor(comment="partly ported from xml to json: see comments")
    public void testLoadDialogFromIssueNavigatorActionCog() throws RemoteException
    {
        loginAsAdmin();
        // ensure one issue
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for dialog action cog test");

        final ShifterDialog shifterDialog = product.getPageBinder()
                .navigateToAndBind(IssueDetailPage.class, issue.getKey())
                .details()
                .openFocusShifter();
        // TODO: select the "Test Issue Action" text (a link with id="<add-on key>__jira-issue-action"),
        // which causes the iframe to be loaded inside a container div with id="embedded-<add-on key>__jira-issue-action",
        // and then look for iframe content by binding to the iframe and calling RemotePluginDialog.wasSubmitted() etc
        ConnectAddOnEmbeddedTestPage page1 = shifterDialog.queryAndSelect("Test Issue Action", ConnectAddOnEmbeddedTestPage.class, remotePlugin.getAddon().getKey(), "jira-issue-action", false);
        ConnectAddOnEmbeddedTestPage page2 = product.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, remotePlugin.getAddon().getKey(), "jira-issue-action", true);

        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, page2);

        assertFalse(dialog.wasSubmitted());
        assertEquals(false, dialog.submit());
        assertTrue(dialog.wasSubmitted());
        assertEquals(true, dialog.submit());
    }

    @Test
    @Ignore("partly ported from xml to json: see comments")
    @XmlDescriptor(comment="partly ported from xml to json: see comments")
    public void testViewIssueTab() throws Exception
    {
        testLoggedInAndAnonymous(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for tab");
                String addOnKey = remotePlugin.getAddon().getKey();
                // TODO: click the link with id="<add-on key>__jira-remote-plugin-issue-tab-page",
                // which loads the iframe contained in the div with id="embedded-<add-on key>__jira-remote-plugin-issue-tab-page",
                // and then look for iframe content with JiraViewIssuePageWithRemotePluginIssueTab.getMessage()
                //JiraViewIssuePageWithRemotePluginIssueTab page = product.visit(
                //        JiraViewIssuePageWithRemotePluginIssueTab.class, issue.getKey(), addOnKey, addOnKey + ":");
                JiraViewIssuePageWithRemotePluginIssueTab page = product.visit(
                        JiraViewIssuePageWithRemotePluginIssueTab.class, "issue-tab-panel", issue.getKey(), addOnKey, ConnectPluginInfo.getPluginKey() + ":");
                Assert.assertEquals("Success", page.getMessage());
                return null;
            }
        });
    }

    @Test
    @Ignore("This test breaks with JIRA 5.2 because of stale page objects. I'm not sure what it tests at all (if anything) so disabling for now.")
    public void testSearchRequestViewPage() throws Exception
    {
        testLoggedInAndAnonymous(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for tab");
                product.visit(AdvancedSearch.class).enterQuery("project = " + project.getKey()).submit();

                PlainTextView plainTextView = product.getPageBinder()
                        .bind(ViewChangingSearchResult.class)
                        .openView("Raw Keys", PlainTextView.class);
                assertTrue(plainTextView.getContent().contains(issue.getKey()));
                return null;
            }
        });
    }

    @Test
    public void testAdminPageInJiraSpecificLocation() throws Exception
    {
        loginAsAdmin();
        String addonKey = remotePlugin.getAddon().getKey();
        product.visit(JiraAdminHomePage.class);

        RemoteWebItem adminPageLink = getAdminPageLink(addonKey, ADVANCED_ADMIN_KEY);

        adminPageLink.click();

        ConnectAddOnEmbeddedTestPage nextPage = connectPageOperations.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, addonKey, ADVANCED_ADMIN_KEY, true);
        assertEquals(ADMIN_FULL_NAME, nextPage.getFullName());
    }

    @Test
    public void testGeneralAdminPage() throws Exception
    {
        loginAsAdmin();
        String addonKey = remotePlugin.getAddon().getKey();
        product.visit(JiraAdminHomePage.class);

        RemoteWebItem adminPageLink = getAdminPageLink(addonKey, ADMIN_KEY);
        adminPageLink.click();

        ConnectAddOnEmbeddedTestPage nextPage = connectPageOperations.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, addonKey, ADMIN_KEY, true);
        assertEquals(ADMIN_FULL_NAME, nextPage.getFullName());
    }

    private RemoteWebItem getAdminPageLink(String addonKey, String adminPageWebItemKey)
    {
        String webitemId = constructAddOnAdminPageLinkId(addonKey, adminPageWebItemKey);
        return connectPageOperations.findWebItem(RemoteWebItem.ItemMatchingMode.ID, webitemId, Optional.<String>absent());
    }

    private String constructAddOnAdminPageLinkId(String addonKey, String adminPageWebItemKey)
    {
        return ModuleKeyUtils.addonAndModuleKey(addonKey, adminPageWebItemKey);
    }

}
