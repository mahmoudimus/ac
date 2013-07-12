package it.jira;

import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.project.BrowseProjectPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailPage;
import com.atlassian.plugin.remotable.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.RemotePluginDialog;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import com.atlassian.plugin.remotable.test.jira.AbstractRemotablePluginProjectTab;
import com.atlassian.plugin.remotable.test.jira.JiraProjectAdministrationTab;
import com.atlassian.plugin.remotable.test.jira.JiraViewIssuePage;
import com.atlassian.plugin.remotable.test.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.remotable.test.jira.PlainTextView;
import com.atlassian.plugin.remotable.test.jira.ViewChangingSearchResult;
import hudson.plugins.jira.soap.RemoteIssue;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class TestJira extends JiraWebDriverTestBase
{
    private static final String REMOTE_PROJECT_CONFIG_TAB_NAME = "Remotable Project Config";

    @Rule
    public HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

    @Ignore("TODO: For some reason, there's an issue in the addLabelViaInlineEdit method where webdriver can't click on the submit button.")
    @Test
    public void testViewIssuePageWithEmbeddedPanelLoggedInWithEdit() throws Exception
    {
        loginAsAdmin();
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey());
//        Assert.assertEquals("Success", viewIssuePage.getMessage());
        viewIssuePage.addLabelViaInlineEdit("foo");
//        Assert.assertEquals("Success", viewIssuePage.getMessage());
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
    public void testAnonymouslyProjectTabWithRestClient() throws Exception
    {
        logout();
        RemotePluginEmbeddedTestPage page = product.visit(BrowseProjectPage.class, project.getKey())
                .openTab(RestClientProjectTabPage.class)
                .getEmbeddedPage();
        Assert.assertEquals("Success", page.getValue("rest-call-status"));
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
    public void testIFrameIsNotPointingToLocalhost() throws Exception
    {
        loginAsAdmin();
        final String baseJiraUrl = product.getProductInstance().getBaseUrl();
        final RemotePluginEmbeddedTestPage page = product.visit(BrowseProjectPage.class, project.getKey())
                .openTab(AppProjectTabPage.class)
                .getEmbeddedPage();
        final String iFrameSrc = page.getContainerDiv().findElement(By.tagName("iframe")).getAttribute("src");
        assertThat(iFrameSrc, startsWith(baseJiraUrl));
    }

//    @Test
//    public void testThatUserTimezoneSettingIsRespectedByRemotablePlugin() throws Exception
//    {
//        final JiraRestClient restClient = createRestClient();
//        try
//        {
//            final DateTime serverTime = restClient.getMetadataClient().getServerInfo().claim().getServerTime();
//            assertNotNull("Expected to get server time using JIRA REST Java Client, but got null instead.", serverTime);
//            final String serverTimeZone = serverTime.getZone().getID();
//
//            // create test user
//            final String testUser = TestConstants.BETTY;
//            backdoor.usersAndGroups().addUserEvenIfUserExists(testUser);
//            backdoor.usersAndGroups().addUserToGroup(testUser, "jira-administrators");
////            loginAs(testUser, testUser);
//
//            // test with a custom timezone
//            final String expectedTimezone = "Africa/Abidjan";
//            product.visit(LoginPage.class).login(TestConstants.BETTY, TestConstants.BETTY, HomePage.class);
//            testTimezoneImpl(expectedTimezone, expectedTimezone, testUser);
//
//            // test with the default timezone
//            testTimezoneImpl(serverTimeZone, "", testUser);
//        }
//        finally
//        {
//            restClient.close();
//        }
//    }

//    private void testTimezoneImpl(final String expectedTimeZone, final String setUserTimeZone, final String testUser)
//    {
//        final RemotePluginAwarePage page;
//        final RemotePluginTestPage remotePluginTest;
//        backdoor.userProfile().setUserTimeZone(testUser, setUserTimeZone);
//        page = product.getPageBinder().bind(JiraGeneralPage.class, REMOTE_PLUGIN_GENERAL_PAGE_KEY, REMOTABLE_PLUGIN_GENERAL_LINK_TEXT);
//        remotePluginTest = page.clickRemotePluginLink();
//        Assert.assertEquals(expectedTimeZone, remotePluginTest.getTimeZone());
//        Assert.assertEquals(expectedTimeZone, remotePluginTest.getTimeZoneFromTemplateContext());
//    }

//    private JiraRestClient createRestClient()
//    {
//        final URI baseUri = URI.create(product.getProductInstance().getBaseUrl());
//        return restClientFactory.createWithBasicHttpAuthentication(baseUri, ADMIN, ADMIN);
//    }

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
                return tab.getName().equals(REMOTE_PROJECT_CONFIG_TAB_NAME);
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
        assertEquals(REMOTE_PROJECT_CONFIG_TAB_NAME, remoteProjectAdministrationTab.getTabs().getSelectedTab().getName());
        Assert.assertEquals(project.getKey(), remoteProjectAdministrationTab.getProjectKey());
        Assert.assertEquals("Success", remoteProjectAdministrationTab.getMessage());
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
