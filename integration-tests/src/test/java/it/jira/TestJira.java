package it.jira;

import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailPage;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.RemotePluginUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnTestPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdministrationHomePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.pageobjects.jira.PlainTextView;
import com.atlassian.plugin.connect.test.pageobjects.jira.ViewChangingSearchResult;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
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
    public static final String EXTRA_PREFIX = "servlet-";
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), RemotePluginUtils.randomPluginKey())
                .setAuthenticationToNone()
                .addModules("adminPages",
                        newPageBean()
                                .withKey("remotePluginAdmin")
                                .withName(new I18nProperty("Remotable Plugin app1 Admin", "admin.page.app1"))
                                .withUrl("/ap")
                                .build(),
                        newPageBean()
                                .withKey("jira-admin-page")
                                .withName(new I18nProperty("Remotable Admin Page", "admin.page"))
                                .withUrl("/jap")
                                .withLocation("advanced_menu_section/advanced_section")
                                .build())
                .addRoute("/ap", ConnectAppServlets.apRequestServlet())
                .addRoute("/jap", ConnectAppServlets.apRequestServlet())
                .addModule("jiraIssueTabPanels",
                        newTabPanelBean()
                                .withKey("jira-remotePluginIssueTabPage")
                                .withName(new I18nProperty("AC Play Issue Tab Page", "issue.tab"))
                                .withUrl("/itp")
                                .build())
                .addRoute("/itp", ConnectAppServlets.apRequestServlet())
                .addModule("webItems",
                        newWebItemBean()
                                .withKey("jira-issueAction")
                                .withName(new I18nProperty("Test Issue Action", "issue.action"))
                                .withUrl("/jia")
                                .withLocation("operations-subtasks")
                                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.dialog).build())
                                .build())
                .addRoute("/jia", ConnectAppServlets.dialogServlet())
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
    public void testLoadDialogFromIssueNavigatorActionCog() throws RemoteException
    {
        loginAsAdmin();
        // ensure one issue
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for dialog action cog test");

        ConnectAddOnTestPage page = product.getPageBinder()
                                           .navigateToAndBind(IssueDetailPage.class, issue.getKey())
                                           .details()
                                           .openFocusShifter()
                                           .queryAndSelect("Test Issue Action", ConnectAddOnTestPage.class, "jira-issue-action", remotePlugin.getAddon().getKey());

        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, page);

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
                String addOnKey = remotePlugin.getAddon().getKey();
                JiraViewIssuePageWithRemotePluginIssueTab page = product.visit(
                        JiraViewIssuePageWithRemotePluginIssueTab.class, issue.getKey(), addOnKey, addOnKey + ":");
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
        final JiraAdministrationHomePage adminPage = product.visit(JiraAdministrationHomePage.class, EXTRA_PREFIX);
        assertTrue(adminPage.hasJiraRemotableAdminPageLink());
        assertEquals(ADMIN_FULL_NAME, adminPage.clickJiraRemotableAdminPage().getFullName());
    }

    @Test
    public void testGeneralAdminPage() throws Exception
    {
        loginAsAdmin();
        final JiraAdministrationHomePage adminPage = product.visit(JiraAdministrationHomePage.class, EXTRA_PREFIX);
        assertTrue(adminPage.hasGeneralRemotableAdminPage());
        assertEquals(ADMIN_FULL_NAME, adminPage.clickGeneralRemotableAdminPage().getFullName());
    }
}
