package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.plugin.connect.test.jira.product.JiraTestedProductAccessor;
import it.jira.project.ProjectForTests;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.rmi.RemoteException;
import java.util.Locale;

public class JiraTestBase {

    private static final String PROJECT_TEMPLATE_KEY = "com.atlassian.jira-core-project-templates:jira-core-task-management";

    protected static JiraTestedProduct product = new JiraTestedProductAccessor().getJiraProduct();

    protected static ProjectForTests project;

    @BeforeClass
    public static void beforeClass() throws RemoteException {
        project = addProject();
    }

    @AfterClass
    public static void afterClass() throws RemoteException {
        product.backdoor().project().deleteProject(project.getKey());
    }

    public static ProjectForTests addProject() {
        String projectKey = RandomStringUtils.randomAlphabetic(4).toUpperCase(Locale.US);
        String projectId = String.valueOf(product.backdoor().project().addProjectWithTemplate(
                "Test project " + projectKey, projectKey, "admin", PROJECT_TEMPLATE_KEY));
        return new ProjectForTests(projectKey, projectId);
    }
}
