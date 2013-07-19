package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.project.BrowseProjectPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailPage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.test.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.remotable.test.pageobjects.RemotePluginTestPage;
import com.atlassian.plugin.remotable.test.pageobjects.jira.AbstractRemotablePluginProjectTab;
import com.atlassian.plugin.remotable.test.pageobjects.jira.JiraAdministrationPage;
import com.atlassian.plugin.remotable.test.pageobjects.jira.JiraOps;
import com.atlassian.plugin.remotable.test.pageobjects.jira.JiraProjectAdministrationPanel;
import com.atlassian.plugin.remotable.test.pageobjects.jira.JiraProjectAdministrationTab;
import com.atlassian.plugin.remotable.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.remotable.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.remotable.test.pageobjects.jira.PlainTextView;
import com.atlassian.plugin.remotable.test.pageobjects.jira.ViewChangingSearchResult;
import com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.remotable.test.server.module.AdminPageModule;
import com.atlassian.plugin.remotable.test.server.module.IssuePanelPageModule;
import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.test.RemotePluginDialog;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import hudson.plugins.jira.soap.RemoteIssue;
import hudson.plugins.jira.soap.RemoteProject;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import static com.atlassian.plugin.remotable.test.Utils.createSignedRequestHandler;
import static com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class TestJira
{
    private static final String EMBEDDED_ISSUE_PANEL_ID = "issue-panel-jira-remotePluginIssuePanelPage";
    private static final String EMBEDDED_PROJECT_CONFIG_PANEL_ID = "project-config-panel-jira-remoteProjectConfigPanel";
    private static final String REMOTABLE_PROEJECT_CONFIG_TAB_NAME = "Remotable Project Config";
    private static final String ADMIN_FULL_NAME = "A. D. Ministrator (Sysadmin)";
    private static final String ADMIN = "admin";

    private static TestedProduct<WebDriverTester> product;
    private static JiraOps jiraOps;
    private static AtlassianConnectAddOnRunner remotePlugin;

    @Rule
    public HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

    private RemoteProject project;

    @BeforeClass
    public static void setupJiraAndStartConnectAddOn() throws Exception
    {
        product = TestedProductFactory.create(JiraTestedProduct.class);
        jiraOps = new JiraOps(product.getProductInstance());
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl(), "app1")
                .addOAuth(createSignedRequestHandler("app1"))
                .addPermission(Permissions.CREATE_OAUTH_LINK)
                .add(AdminPageModule.key("remotePluginAdmin")
                        .name("Remotable Plugin app1 Admin")
                        .path("/ap")
                        .resource(newMustacheServlet("iframe.mu")))
                .add(AdminPageModule.key("jira-admin-page")
                        .name("Remotable Admin Page")
                        .path("/jap")
                        .section("advanced_menu_section/advanced_section")
                        .resource(newMustacheServlet("iframe.mu")))
                .add(IssuePanelPageModule.key("jira-remotePluginIssuePanelPage")
                        .name("AC Play Issue Page Panel")
                        .path("/ipp")
                        .resource(newMustacheServlet("iframe.mu")))
                .addIssueTabPage("jira-remotePluginIssueTabPage", "AC Play Issue Tab Page", "/itp", newMustacheServlet("iframe.mu"))
                .addProjectTabPage("jira-remotePluginProjectTab", "AC Play Project Tab", "/ptp", newMustacheServlet("iframe.mu"))
                .addProjectConfigPanel("jira-remoteProjectConfigPanel", "AC Play Project Config Panel", "/pcp", newMustacheServlet("iframe.mu"))
                .addProjectConfigTab("jira-remotePluginProjectConfigTab", "Remotable Project Config", "/pct", newMustacheServlet("iframe.mu"))
                .addDialogPage("jira-issueAction", "Test Issue Action", "/jia", newMustacheServlet("dialog.mu"), "operations-subtasks")
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stop();
        }
    }

    @Before
    public void createProject() throws Exception
    {
        project = jiraOps.createProject();
    }

    @After
    public void deleteProject() throws Exception
    {
        if (project != null)
        {
            jiraOps.deleteProject(project.getKey());
        }
    }

    @After
    public void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }


    private void loginAsAdmin()
    {
        loginAs(ADMIN, ADMIN);
    }

    private void loginAs(String username, String password)
    {
        product.visit(LoginPage.class).login(username, password, DashboardPage.class);
    }


    @Test
    public void testViewIssuePageWithEmbeddedPanelAnonymous() throws Exception
    {
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey(), EMBEDDED_ISSUE_PANEL_ID);
        Assert.assertEquals("Success", viewIssuePage.getMessage());
    }

    @Ignore("TODO: For some reason, there's an issue in the addLabelViaInlineEdit method where webdriver can't click on the submit button.")
    @Test
    public void testViewIssuePageWithEmbeddedPanelLoggedInWithEdit() throws Exception
    {
        loginAsAdmin();
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey(),
                EMBEDDED_ISSUE_PANEL_ID);
        Assert.assertEquals("Success", viewIssuePage.getMessage());
        viewIssuePage.addLabelViaInlineEdit("foo");
        Assert.assertEquals("Success", viewIssuePage.getMessage());
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
    public void testProjectTab() throws Exception
    {
        testLoggedInAndAnonymous(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                RemotePluginEmbeddedTestPage page = product.visit(BrowseProjectPage.class, project.getKey())
                        .openTab(AppProjectTabPage.class)
                        .getEmbeddedPage();

                Assert.assertEquals("Success", page.getMessage());
                return null;
            }
        });
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
                        JiraViewIssuePageWithRemotePluginIssueTab.class, issue.getKey());
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
    public void testViewProjectAdminPanel() throws Exception
    {
        loginAsAdmin();
        product.visit(ProjectSummaryPageTab.class, project.getKey());
        JiraProjectAdministrationPanel webPanel = product.visit(JiraProjectAdministrationPanel.class,
                EMBEDDED_PROJECT_CONFIG_PANEL_ID, project.getKey());
        Assert.assertEquals("Success", webPanel.getMessage());
    }

    @Test
    public void testAdminPageInJiraSpecificLocation() throws Exception
    {
        loginAsAdmin();
        final JiraAdministrationPage adminPage = product.visit(JiraAdministrationPage.class);
        assertTrue(adminPage.hasJiraRemotableAdminPageLink());
        assertEquals(ADMIN_FULL_NAME, adminPage.clickJiraRemotableAdminPage().getFullName());
    }

    @Test
    public void testGeneralAdminPage() throws Exception
    {
        loginAsAdmin();
        final JiraAdministrationPage adminPage = product.visit(JiraAdministrationPage.class);
        assertTrue(adminPage.hasGeneralRemotableAdminPage());
        assertEquals(ADMIN_FULL_NAME, adminPage.clickGeneralRemotableAdminPage().getFullName());
    }

    @Test
    public void testViewProjectAdminTab() throws Exception
    {
        loginAsAdmin();
        final ProjectSummaryPageTab page =
                product.visit(ProjectSummaryPageTab.class, project.getKey());

        assertThat(page.getTabs().getTabs(), hasItem(new TypeSafeMatcher<ProjectConfigTabs.Tab>()
        {

            @Override
            public boolean matchesSafely(final ProjectConfigTabs.Tab tab)
            {
                return tab.getName().equals(REMOTABLE_PROEJECT_CONFIG_TAB_NAME);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Project Configuration Tabs should contain Remotable Project Config tab");
            }
        }));

        final JiraProjectAdministrationTab remoteProjectAdministrationTab =
                page.getTabs().gotoTab(
                        "webitem-jira-remotePluginProjectConfigTab",
                        JiraProjectAdministrationTab.class,
                        project.getKey());

        // Test of workaround for JRA-26407.
        assertNotNull(remoteProjectAdministrationTab.getProjectHeader());
        assertEquals(REMOTABLE_PROEJECT_CONFIG_TAB_NAME, remoteProjectAdministrationTab.getTabs().getSelectedTab().getName());
        Assert.assertEquals(project.getKey(), remoteProjectAdministrationTab.getProjectKey());
        Assert.assertEquals("Success", remoteProjectAdministrationTab.getMessage());
    }

    private void testLoggedInAndAnonymous(Callable runnable) throws Exception
    {
        loginAsAdmin();
        runnable.call();
        logout();
        runnable.call();
    }

    public static final class AppProjectTabPage extends AbstractRemotablePluginProjectTab
    {
        public AppProjectTabPage(final String projectKey)
        {
            super(projectKey, "project-tab-jira-remotePluginProjectTab");
        }
    }
}
