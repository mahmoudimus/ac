package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.testkit.client.IssueTypeControl;
import com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider;
import it.util.TestProject;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.rmi.RemoteException;
import java.util.Locale;

public class JiraTestBase
{

    protected static JiraTestedProduct product = TestedProductProvider.getJiraTestedProduct();

    protected static TestProject project;

    @BeforeClass
    public static void beforeClass() throws RemoteException
    {
        project = addProject();
        deleteAllIssueTypes();
    }

    @AfterClass
    public static void afterClass() throws RemoteException
    {
        product.backdoor().project().deleteProject(project.getKey());
    }

    public static TestProject addProject()
    {
        String projectKey = RandomStringUtils.randomAlphabetic(4).toUpperCase(Locale.US);
        String projectId = String.valueOf(product.backdoor().project().addProjectWithTemplate(
                "Test project " + projectKey, projectKey, "admin", "com.atlassian.jira-core-project-templates:jira-issuetracking"));
        return new TestProject(projectKey, projectId);
    }

    public static void deleteAllIssueTypes()
    {
        IssueTypeControl issueTypeControl = product.backdoor().issueType();
        for (IssueTypeControl.IssueType issueType : issueTypeControl.getIssueTypes())
        {
            issueTypeControl.deleteIssueType(Long.valueOf(issueType.getId()));
        }
    }
}
