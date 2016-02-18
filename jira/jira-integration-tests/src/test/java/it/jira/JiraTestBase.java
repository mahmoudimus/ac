package it.jira;

import java.rmi.RemoteException;
import java.util.Locale;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.plugin.connect.test.jira.product.JiraTestedProductAccessor;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import it.jira.project.TestProject;

public class JiraTestBase
{

    private static final String PROJECT_TEMPLATE_KEY = "com.atlassian.jira-core-project-templates:jira-core-task-management";

    protected static JiraTestedProduct product = new JiraTestedProductAccessor().getJiraProduct();

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
                "Test project " + projectKey, projectKey, "admin", PROJECT_TEMPLATE_KEY));
        return new TestProject(projectKey, projectId);
    }
}
