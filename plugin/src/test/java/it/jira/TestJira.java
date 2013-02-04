package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.project.BrowseProjectPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.RemotePluginDialog;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import com.atlassian.plugin.remotable.test.jira.JiraIssueNavigatorPage;
import com.atlassian.plugin.remotable.test.jira.JiraOps;
import com.atlassian.plugin.remotable.test.jira.JiraProjectAdministrationPanel;
import com.atlassian.plugin.remotable.test.jira.JiraProjectAdministrationTab;
import com.atlassian.plugin.remotable.test.jira.JiraRemotablePluginProjectTab;
import com.atlassian.plugin.remotable.test.jira.JiraViewIssuePage;
import com.atlassian.plugin.remotable.test.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.remotable.test.jira.PlainTextView;
import com.atlassian.plugin.remotable.test.jira.ViewChangingSearchResult;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import hudson.plugins.jira.soap.RemoteAuthenticationException;
import hudson.plugins.jira.soap.RemoteIssue;
import hudson.plugins.jira.soap.RemoteProject;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

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
    private static TestedProduct<WebDriverTester> product = TestedProductFactory.create(JiraTestedProduct.class);
    private static JiraOps jiraOps = new JiraOps(product.getProductInstance());

    public static final String ADMIN = "admin";


    @Rule
    public MethodRule rule = new HtmlDumpRule(product.getTester().getDriver());
    private RemoteProject project;

    @After
    public void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    @Before
    public void setUp() throws RemoteException, RemoteAuthenticationException
    {
        project = jiraOps.createProject();

    }

    private void loginAsAdmin()
    {
        product.visit(LoginPage.class).login(ADMIN, ADMIN, DashboardPage.class);
    }

    @After
    public void tearDown() throws RemoteException
    {
        jiraOps.deleteProject(project.getKey());
    }

    @Test
    public void testViewIssuePageWithEmbeddedPanelAnonymous() throws Exception
    {
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey(),
                EMBEDDED_ISSUE_PANEL_ID);
        assertEquals("Success", viewIssuePage.getMessage());
    }

    @Test
    public void testViewIssuePageWithEmbeddedPanelLoggedInWithEdit() throws Exception
    {
        loginAsAdmin();
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey(),
                EMBEDDED_ISSUE_PANEL_ID);
        assertEquals("Success", viewIssuePage.getMessage());
        viewIssuePage.addLabelViaInlineEdit("foo");
        assertEquals("Success", viewIssuePage.getMessage());
    }

    @Test
    public void testLoadDialogFromIssueNavigatorActionCog() throws RemoteException
    {
        loginAsAdmin();
        // ensure one issue
        jiraOps.createIssue(project.getKey(), "Test issue for dialog action cog test");
        RemotePluginTestPage page = product.getPageBinder().navigateToAndBind(JiraIssueNavigatorPage.class)
                .getResults()
                .nextIssue()
                .openActionsDialog()
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
                                                           .openTab(JiraRemotablePluginProjectTab.class)
                                                           .getEmbeddedPage();

                assertEquals("Success", page.getMessage());
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
                assertEquals("Success", page.getMessage());
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
        testLoggedInAsAdmin(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                ProjectSummaryPageTab page =
                        product.visit(ProjectSummaryPageTab.class, project.getKey());
                assertThat(page.getPanelHeadingTexts(), hasItem("Remote Project Config Panel"));
                JiraProjectAdministrationPanel webPanel = product.visit(JiraProjectAdministrationPanel.class,
                        EMBEDDED_PROJECT_CONFIG_PANEL_ID, project.getKey());
                assertEquals("Success", webPanel.getMessage());
                return null;
            }
        });
    }

    @Test
    public void testViewProjectAdminTab() throws Exception
    {
        testLoggedInAsAdmin(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
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
                assertEquals(project.getKey(), remoteProjectAdministrationTab.getProjectKey());
                assertEquals("Success", remoteProjectAdministrationTab.getMessage());

                return null;
            }
        });
    }

    private void testLoggedInAndAnonymous(Callable runnable) throws Exception
    {
        loginAsAdmin();
        runnable.call();
        logout();
        runnable.call();
    }

    private void testLoggedInAsAdmin(Callable runnable) throws Exception
    {
        loginAsAdmin();
        runnable.call();
    }
}
