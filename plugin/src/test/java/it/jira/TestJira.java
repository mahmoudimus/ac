package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.project.BrowseProjectPage;
import com.atlassian.plugin.remotable.test.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.remotable.test.RemotePluginRunner;
import com.atlassian.plugin.remotable.test.jira.*;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import hudson.plugins.jira.soap.RemoteAuthenticationException;
import hudson.plugins.jira.soap.RemoteIssue;
import hudson.plugins.jira.soap.RemoteProject;
import org.apache.http.client.HttpResponseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class TestJira
{
    private static final String EMBEDDED_ISSUE_PANEL_ID = "issue-panel-jira-remotePluginIssuePanelPage";
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
	public void testViewIssuePageWithEmbeddedPanel() throws Exception
    {
        testLoggedInAndAnonymous(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
                JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class,
                        issue.getKey(),
                        EMBEDDED_ISSUE_PANEL_ID);
                assertEquals("Success", viewIssuePage.getMessage());
                return null;
            }
        });

	}

    @Test
    public void testProjectTab() throws Exception
    {
        testLoggedInAndAnonymous(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                RemotePluginEmbeddedTestPage page = product.visit(BrowseProjectPage.class,
                        project.getKey())
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
    public void testSearchRequestViewPage() throws Exception
    {
        testLoggedInAndAnonymous(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for tab");
                product.visit(AdvancedSearch.class)
                        .enterQuery("project = " + project.getKey())
                        .submit();

                PlainTextView plainTextView = product.getPageBinder().bind(
                        ViewChangingSearchResult.class)
                        .openView("Raw Keys", PlainTextView.class);
                assertTrue(plainTextView.getContent().contains(issue.getKey()));
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
}
