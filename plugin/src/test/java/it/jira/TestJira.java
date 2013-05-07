package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.ProductInstanceBasedEnvironmentData;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.project.BrowseProjectPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailPage;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.RemotePluginAwarePage;
import com.atlassian.plugin.remotable.test.RemotePluginDialog;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import com.atlassian.plugin.remotable.test.jira.AbstractRemotablePluginProjectTab;
import com.atlassian.plugin.remotable.test.jira.JiraGeneralPage;
import com.atlassian.plugin.remotable.test.jira.JiraOps;
import com.atlassian.plugin.remotable.test.jira.JiraProjectAdministrationPanel;
import com.atlassian.plugin.remotable.test.jira.JiraProjectAdministrationTab;
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
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.openqa.selenium.By;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import static org.hamcrest.Matchers.startsWith;
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
    public static final String REMOTABLE_PLUGIN_GENERAL_LINK_TEXT = "Remotable Plugin app1 General Link";
    public static final String REMOTE_PLUGIN_GENERAL_PAGE_KEY = "remotePluginGeneral";
    private static TestedProduct<WebDriverTester> product = TestedProductFactory.create(JiraTestedProduct.class);
    private static JiraOps jiraOps = new JiraOps(product.getProductInstance());

    public static final String ADMIN = "admin";


    @Rule
    public MethodRule rule = new HtmlDumpRule(product.getTester().getDriver());
    private RemoteProject project;
    private final AsynchronousJiraRestClientFactory restClientFactory = new AsynchronousJiraRestClientFactory();
    private final Backdoor backdoor = new Backdoor(new ProductInstanceBasedEnvironmentData(product.getProductInstance()));

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
        loginAs(ADMIN, ADMIN);
    }

    private void loginAs(String username, String password) {
        product.visit(LoginPage.class).login(username, password, DashboardPage.class);
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

                assertEquals("Success", page.getMessage());
                return null;
            }
        });

    }

    @Test
    public void testAnonymouslyProjectTabWithRestClient() throws Exception
    {
        testAnonymous(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                RemotePluginEmbeddedTestPage page = product.visit(BrowseProjectPage.class, project.getKey())
                        .openTab(RestClientProjectTabPage.class)
                        .getEmbeddedPage();
                assertEquals("Success", page.getValue("rest-call-status"));
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
                JiraProjectAdministrationPanel webPanel = product.visit(JiraProjectAdministrationPanel.class,
                        EMBEDDED_PROJECT_CONFIG_PANEL_ID, project.getKey());
                assertEquals("Success", webPanel.getMessage());
                return null;
            }
        });
    }

    @Test
    public void testIFrameIsNotPointingToLocalhost() throws Exception
    {
        testLoggedInAsAdmin(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                final String baseJiraUrl = product.getProductInstance().getBaseUrl();
                final RemotePluginEmbeddedTestPage page = product.visit(BrowseProjectPage.class, project.getKey())
                        .openTab(AppProjectTabPage.class)
                        .getEmbeddedPage();
                final String iFrameSrc = page.getContainerDiv().findElement(By.tagName("iframe")).getAttribute("src");
                assertThat(iFrameSrc, startsWith(baseJiraUrl));
                return null;
            }
        });
    }

    @Test
    public void testThatUserTimezoneSettingIsRespectedByRemotablePlugin() throws Exception
    {
        final JiraRestClient restClient = createRestClient();
        try
        {
            final DateTime serverTime = restClient.getMetadataClient().getServerInfo().claim().getServerTime();
            assertNotNull("Expected to get server time using JIRA REST Java Client, but got null instead.", serverTime);
            final String serverTimeZone = serverTime.getZone().getID();

            // create test user
            final String testUser = "betty";
            backdoor.usersAndGroups().addUserEvenIfUserExists(testUser);
            backdoor.usersAndGroups().addUserToGroup(testUser, "jira-administrators");
//            loginAs(testUser, testUser);

            // test with a custom timezone
            final String expectedTimezone = "Africa/Abidjan";
            product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
            testTimezoneImpl(expectedTimezone, expectedTimezone, testUser);

            // test with the default timezone
            testTimezoneImpl(serverTimeZone, "", testUser);
        }
        finally
        {
            restClient.close();
        }
    }

    private void testTimezoneImpl(final String expectedTimeZone, final String setUserTimeZone, final String testUser)
    {
        final RemotePluginAwarePage page;
        final RemotePluginTestPage remotePluginTest;
        backdoor.userProfile().setUserTimeZone(testUser, setUserTimeZone);
        page = product.getPageBinder().bind(JiraGeneralPage.class, REMOTE_PLUGIN_GENERAL_PAGE_KEY, REMOTABLE_PLUGIN_GENERAL_LINK_TEXT);
        remotePluginTest = page.clickRemotePluginLink();
        assertEquals(expectedTimeZone, remotePluginTest.getTimeZone());
        assertEquals(expectedTimeZone, remotePluginTest.getTimeZoneFromTemplateContext());
    }

    private JiraRestClient createRestClient()
    {
        final URI baseUri = URI.create(product.getProductInstance().getBaseUrl());
        return restClientFactory.createWithBasicHttpAuthentication(baseUri, ADMIN, ADMIN);
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

    private void testAnonymous(Callable runnable) throws Exception
    {
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

    public static final class RestClientProjectTabPage extends AbstractRemotablePluginProjectTab
    {

        public RestClientProjectTabPage(final String projectKey)
        {
            super(projectKey, "project-tab-jira-restClientProjectTab");
        }
    }
}
