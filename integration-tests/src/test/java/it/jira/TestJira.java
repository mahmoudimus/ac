package it.jira;

import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailPage;
import com.atlassian.plugin.remotable.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.RemotePluginDialog;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import com.atlassian.plugin.remotable.test.jira.JiraAdministrationPage;
import com.atlassian.plugin.remotable.test.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.remotable.test.jira.PlainTextView;
import com.atlassian.plugin.remotable.test.jira.ViewChangingSearchResult;
import hudson.plugins.jira.soap.RemoteIssue;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import static it.TestConstants.ADMIN_FULL_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestJira extends JiraWebDriverTestBase
{
    @Rule
    public HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

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
    public void testAdminPageInJiraSpecificLocation() throws Exception {
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

}
