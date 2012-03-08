package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.labs.remoteapps.test.HtmlDumpRule;
import com.atlassian.labs.remoteapps.test.jira.JiraOps;
import com.atlassian.labs.remoteapps.test.jira.JiraViewIssuePage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import hudson.plugins.jira.soap.RemoteAuthenticationException;
import hudson.plugins.jira.soap.RemoteIssue;
import hudson.plugins.jira.soap.RemoteProject;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import java.rmi.RemoteException;

import static com.atlassian.labs.remoteapps.test.RemoteAppUtils.waitForEvent;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class TestJira
{
    private static final String EMBEDDED_ISSUE_TAB_PAGE_ID = "issue-tab-page-remoteAppIssueTabPage";
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
        product.visit(LoginPage.class).login(ADMIN, ADMIN, DashboardPage.class);
        
    }
    
    @After
    public void tearDown() throws RemoteException
    {
        jiraOps.deleteProject(project.getKey());
    }
    @Test
	public void testViewIssuePageWithEmbeddedPanel() throws InterruptedException, RemoteException,
                                                            RemoteAuthenticationException
    {
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey(), EMBEDDED_ISSUE_TAB_PAGE_ID);
        assertEquals("Success", viewIssuePage.getMessage());
	}

    @Test
    public void testIssueCreatedWebHookFired() throws Exception
    {
        String issueKey = jiraOps.createIssue(project.getKey(), "Test issue").getKey();
        JSONObject issue = null;
        for (int x=0; x<5; x++)
        {
            JSONObject event = waitForEvent(product.getProductInstance(), "issue_created");
            issue = event.getJSONObject("issue");
            if (issueKey.equals(issue.getString("key")))
            {
                break;
            }
        }

        assertNotNull(issue);
        assertEquals(ADMIN, issue.getString("reporterName"));
    }
}
