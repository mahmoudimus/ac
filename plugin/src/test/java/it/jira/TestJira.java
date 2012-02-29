package it.jira;

import com.atlassian.jira.pageobjects.project.DeleteProjectPage;
import com.atlassian.jira.pageobjects.project.ViewProjectsPage;
import com.atlassian.labs.remoteapps.test.HtmlDumpRule;
import com.atlassian.labs.remoteapps.test.jira.JiraCreateIssuePage;
import com.atlassian.labs.remoteapps.test.jira.JiraViewIssuePage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import static org.junit.Assert.assertEquals;

public class TestJira
{
    private static final String EMBEDDED_ISSUE_TAB_PAGE_ID = "issue-tab-page-remoteAppIssueTabPage";

    private static TestedProduct<WebDriverTester> product = TestedProductFactory.create(com.atlassian.webdriver.jira.JiraTestedProduct.class);

    @Rule
    public MethodRule rule = new HtmlDumpRule(product.getTester().getDriver());

    @After
    public void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    @Test
	public void testViewIssuePageWithEmbeddedPanel() throws InterruptedException
    {
        product.visit(LoginPage.class).login("admin", "admin", HomePage.class);
        long projectId = createProject();
        String issueKey = createIssue(projectId);

        try
        {
            JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issueKey, EMBEDDED_ISSUE_TAB_PAGE_ID);
            assertEquals("Success", viewIssuePage.getMessage());
        }
        finally
        {
            deleteProject(projectId);
        }
	}

    public void deleteProject(long projectId)
    {
        product.visit(DeleteProjectPage.class, projectId)
            .submitConfirm();
    }

    public String createIssue(final long projectId)
    {
        String issueKey = product.visit(JiraCreateIssuePage.class, projectId)
            .summary("Please prevent the heat death of the universe ASAP")
            .submit()
            .readKeyFromPage();
        return issueKey;
    }

    private long createProject()
    {
        String projectKey = RandomStringUtils.randomAlphabetic(4);
        String projectName = "Project " + projectKey;

        return product.visit(ViewProjectsPage.class)
            .openCreateProjectDialog()
            .setKey(projectKey)
            .setName(projectName)
            .submitSuccess()
            .getProjectId();
    }
}
