package it.jira;

import java.rmi.RemoteException;
import java.util.Locale;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import it.util.TestProject;

public class JiraTestBase
{

    protected static JiraTestedProduct product = TestedProductProvider.getJiraTestedProduct();

    protected static TestProject project;

    @BeforeClass
    public static void beforeClass() throws RemoteException
    {
        project = addProject();
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
}
