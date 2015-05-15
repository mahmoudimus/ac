package it.jira;

import com.atlassian.confluence.it.TestUserFactory;
import com.atlassian.jira.pageobjects.dialogs.ShifterDialog;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage;
import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailPage;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.tests.TestBase;
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
import it.util.ConnectTestUserFactory;
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
    private static final String JIRA_ISSUE_ACTION_KEY = "jira-issue-action";

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
                                .withKey(JIRA_ISSUE_ACTION_KEY)
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
        TestUser user = testUserFactory.basicUser();
        login(user);
        // ensure one issue
        IssueCreateResponse issue = TestBase.funcTestHelper.backdoor.issues().createIssue(projectKey, "Test issue for dialog action cog test");
        

        final ShifterDialog shifterDialog = product.getPageBinder()
                .navigateToAndBind(IssueDetailPage.class, issue.key)
                .details()
                .openFocusShifter();
        ConnectAddOnEmbeddedTestPage page = shifterDialog.queryAndSelect("Test Issue Action", ConnectAddOnEmbeddedTestPage.class, runner.getAddon().getKey(), JIRA_ISSUE_ACTION_KEY, true);
        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, page);

        assertFalse(dialog.wasSubmitted());
        dialog.submitAndWaitUntilSubmitted();
        dialog.submitAndWaitUntilHidden();
    }

    @Test
    public void testViewIssueTab() throws Exception
    {
        testLoggedInAndAnonymous(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                IssueCreateResponse issue = TestBase.funcTestHelper.backdoor.issues().createIssue(projectKey, "Test issue for tab");
                String addOnKey = runner.getAddon().getKey();
                JiraViewIssuePageWithRemotePluginIssueTab page = product.visit(
                        JiraViewIssuePageWithRemotePluginIssueTab.class, ISSUE_TAB_PANEL_KEY, issue.key, addOnKey);
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
                IssueCreateResponse issue = TestBase.funcTestHelper.backdoor.issues().createIssue(projectKey, "Test issue for tab");
                product.visit(AdvancedSearch.class).enterQuery("project = " + projectKey).submit();

                PlainTextView plainTextView = product.getPageBinder()
                        .bind(ViewChangingSearchResult.class)
                        .openView("Raw Keys", PlainTextView.class);
                assertTrue(plainTextView.getContent().contains(issue.key));
                return null;
            }
        });
    }

    @Test
    public void testAdminPageInJiraSpecificLocation() throws Exception
    {
        TestUser user = testUserFactory.admin();
        String addonKey = runner.getAddon().getKey();
        loginAndVisit(user, ViewGeneralConfigurationPage.class);

        RemoteWebItem adminPageLink = getAdminPageLink(addonKey, ADVANCED_ADMIN_KEY);

        adminPageLink.click();

        ConnectAddOnEmbeddedTestPage nextPage = connectPageOperations.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, addonKey, ADVANCED_ADMIN_KEY, true);
        assertEquals(user.getDisplayName(), nextPage.getFullName());
    }

    @Test
    public void testGeneralAdminPage() throws Exception
    {
        TestUser user = testUserFactory.admin();
        String addonKey = runner.getAddon().getKey();
        loginAndVisit(user, ViewGeneralConfigurationPage.class);

        RemoteWebItem adminPageLink = getAdminPageLink(addonKey, ADMIN_KEY);
        adminPageLink.click();

        ConnectAddOnEmbeddedTestPage nextPage = connectPageOperations.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, addonKey, ADMIN_KEY, true);
        assertEquals(user.getDisplayName(), nextPage.getFullName());
    }

    private RemoteWebItem getAdminPageLink(String addonKey, String adminPageWebItemKey)
    {
        String webitemId = ModuleKeyUtils.addonAndModuleKey(addonKey, adminPageWebItemKey);
        return connectPageOperations.findWebItem(RemoteWebItem.ItemMatchingMode.ID, webitemId, Optional.<String>absent());
    }
}
