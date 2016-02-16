package it.jira.util;

import java.util.Locale;
import java.util.stream.Stream;

import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.plugin.connect.test.jira.product.JiraTestedProductAccessor;
import it.jira.project.TestProject;
import org.apache.commons.lang.RandomStringUtils;

public class JiraTestHelper
{
    private static final String PROJECT_TEMPLATE_KEY = "com.atlassian.jira-core-project-templates:jira-core-task-management";

    protected static JiraTestedProduct product = new JiraTestedProductAccessor().getJiraProduct();

    private static final NavigationFactory navigationFactory = new NavigationFactory(product.environmentData());


    public static TestProject addProject()
    {
        String projectKey = RandomStringUtils.randomAlphabetic(4).toUpperCase(Locale.US);
        String projectId = String.valueOf(product.backdoor().project().addProjectWithTemplate(
                "Test project " + projectKey, projectKey, "admin", PROJECT_TEMPLATE_KEY));
        return new TestProject(projectKey, projectId);
    }

    public static void addUserIfDoesNotExist(String username, String... groups)
    {
        boolean userExists = product.backdoor().usersAndGroups().getAllUsers().stream().filter(user -> user.getUsername().equals(username)).findFirst().isPresent();
        if (!userExists)
        {
            product.backdoor().usersAndGroups().addUser(username);
            Stream.of(groups).forEach(group ->
                    product.backdoor().usersAndGroups().addUserToGroup(username, group));
        }
    }

    public static Navigation getNavigation() {
        return navigationFactory.createNavigation();
    }

    public static MailTestHelper setUpMailTest() {
        return new MailTestHelper(product.backdoor()).configure();
    }
}
