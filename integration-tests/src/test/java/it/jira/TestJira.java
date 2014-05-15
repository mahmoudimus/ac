package it.jira;

import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdministrationHomePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.pageobjects.jira.PlainTextView;
import com.atlassian.plugin.connect.test.pageobjects.jira.ViewChangingSearchResult;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.AdminPageModule;
import com.atlassian.plugin.connect.test.server.module.DialogPageModule;
import com.atlassian.plugin.connect.test.server.module.IssueTabPageModule;
import hudson.plugins.jira.soap.RemoteIssue;
import it.servlet.ConnectAppServlets;
import org.junit.*;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import static it.TestConstants.ADMIN_FULL_NAME;
import static org.junit.Assert.*;

public class TestJira extends JiraWebDriverTestBase
{
    public static final String EXTRA_PREFIX = "servlet-";
    private static AtlassianConnectAddOnRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .add(AdminPageModule.key("remotePluginAdmin")
                                    .name("Remotable Plugin app1 Admin")
                                    .path("/ap")
                                    .resource(ConnectAppServlets.apRequestServlet()))
                .add(AdminPageModule.key("jira-admin-page")
                                    .name("Remotable Admin Page")
                                    .path("/jap")
                                    .section("advanced_menu_section/advanced_section")
                                    .resource(ConnectAppServlets.apRequestServlet()))
                .add(IssueTabPageModule.key("jira-remotePluginIssueTabPage")
                                       .name("AC Play Issue Tab Page")
                                       .path("/itp")
                                       .resource(ConnectAppServlets.apRequestServlet()))
                .add(DialogPageModule.key("jira-issueAction")
                                     .name("Test Issue Action")
                                     .path("/jia")
                                     .section("operations-subtasks")
                                     .resource(ConnectAppServlets.dialogServlet()))
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

        RemotePluginTestPage page = product.getPageBinder()
                                           .navigateToAndBind(IssueDetailPage.class, issue.getKey())
                                           .details()
                                           .openFocusShifter()
                                           .queryAndSelect("Test Issue Action", RemotePluginTestPage.class, "jira-issueAction");

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
                JiraViewIssuePageWithRemotePluginIssueTab page = product.visit(
                        JiraViewIssuePageWithRemotePluginIssueTab.class, issue.getKey(), remotePlugin.getPluginKey(), remotePlugin.getPluginKey() + ":");
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
