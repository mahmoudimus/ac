package it.jira;

import com.atlassian.jira.pageobjects.dialogs.ShifterDialog;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage;
import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailPage;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
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
import it.util.TestUser;
import org.junit.*;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static org.junit.Assert.*;

public class TestJira extends JiraWebDriverTestBase
{
    private static final String ADMIN_KEY = "addon-admin";
    private static final String ADVANCED_ADMIN_KEY = "advanced-addon-admin";
    private static final String ISSUE_TAB_PANEL_KEY = "issue-tab-panel";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
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
                                .withKey(ISSUE_TAB_PANEL_KEY)
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
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void testLoadDialogFromIssueNavigatorActionCog() throws RemoteException
    {
        login(TestUser.ADMIN);
        // ensure one issue
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for dialog action cog test");

        final ShifterDialog shifterDialog = product.getPageBinder()
                .navigateToAndBind(IssueDetailPage.class, issue.getKey())
                .details()
                .openFocusShifter();
        // TODO: select the "Test Issue Action" text (a link with id="<add-on key>__jira-issue-action"),
        // which causes the iframe to be loaded inside a container div with id="embedded-<add-on key>__jira-issue-action",
        // and then look for iframe content by binding to the iframe and calling RemotePluginDialog.wasSubmitted() etc
        ConnectAddOnEmbeddedTestPage page1 = shifterDialog.queryAndSelect("Test Issue Action", ConnectAddOnEmbeddedTestPage.class, runner.getAddon().getKey(), "jira-issue-action", false);
        ConnectAddOnEmbeddedTestPage page2 = product.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, runner.getAddon().getKey(), "jira-issue-action", true);

        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, page2);

        assertFalse(dialog.wasSubmitted());
        assertEquals(false, dialog.submit());
        assertTrue(dialog.wasSubmitted());
        assertEquals(true, dialog.submit());
    }

    @Test
    public void testViewIssueTab() throws Exception
    {
        testLoggedInAndAnonymous(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for tab");
                String addOnKey = runner.getAddon().getKey();
                JiraViewIssuePageWithRemotePluginIssueTab page = product.visit(
                        JiraViewIssuePageWithRemotePluginIssueTab.class, ISSUE_TAB_PANEL_KEY, issue.getKey(), addOnKey);
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
        String addonKey = runner.getAddon().getKey();
        loginAndVisit(TestUser.ADMIN, ViewGeneralConfigurationPage.class);

        RemoteWebItem adminPageLink = getAdminPageLink(addonKey, ADVANCED_ADMIN_KEY);

        adminPageLink.click();

        ConnectAddOnEmbeddedTestPage nextPage = connectPageOperations.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, addonKey, ADVANCED_ADMIN_KEY, true);
        assertEquals(TestUser.ADMIN.getDisplayName(), nextPage.getFullName());
    }

    @Test
    public void testGeneralAdminPage() throws Exception
    {
        String addonKey = runner.getAddon().getKey();
        loginAndVisit(TestUser.ADMIN, ViewGeneralConfigurationPage.class);

        RemoteWebItem adminPageLink = getAdminPageLink(addonKey, ADMIN_KEY);
        adminPageLink.click();

        ConnectAddOnEmbeddedTestPage nextPage = connectPageOperations.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, addonKey, ADMIN_KEY, true);
        assertEquals(TestUser.ADMIN.getDisplayName(), nextPage.getFullName());
    }

    private RemoteWebItem getAdminPageLink(String addonKey, String adminPageWebItemKey)
    {
        String webitemId = ModuleKeyUtils.addonAndModuleKey(addonKey, adminPageWebItemKey);
        return connectPageOperations.findWebItem(RemoteWebItem.ItemMatchingMode.ID, webitemId, Optional.<String>absent());
    }
}
