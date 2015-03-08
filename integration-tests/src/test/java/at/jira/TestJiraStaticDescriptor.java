package at.jira;

import java.util.List;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.Project;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.google.common.collect.Iterables.any;
import static it.util.TestUser.ADMIN;

@Category (OnDemandAcceptanceTest.class)
public class TestJiraStaticDescriptor extends JiraAcceptanceTestBase
{
    private static final String PROJECT_KEY = "ACTEST";
    private static final String PROJECT_NAME = "Atlassian Connect Test";
    private static final String WEB_ITEM_ID = "com.atlassian.connect.acceptance.test__opsbar-test-web-item";

    @BeforeClass
    public static void installAddon() throws Exception
    {
        installer.install();
    }

    @AfterClass
    public static void uninstallAddon() throws Exception
    {
        installer.uninstall();
    }

    @Test
    public void testAcActionWebItemIsPresent()
    {
        createConnectProject();
        IssueCreateResponse response = product.backdoor().issues()
                .createIssue(PROJECT_KEY, "Atlassian Connect Web Panel Test Issue");

        product.quickLogin(ADMIN.getUsername(), ADMIN.getPassword());
        product.goToViewIssue(response.key());

        connectPageOperations.findWebItem(WEB_ITEM_ID, Optional.<String>absent());
    }

    private void createConnectProject()
    {
        List<Project> projects = product.backdoor().project().getProjects();
        boolean projectExists = any(projects, new Predicate<Project>()
        {
            @Override
            public boolean apply(Project project)
            {
                return PROJECT_KEY.equals(project.key);
            }
        });

        if (!projectExists)
        {
            product.backdoor().project().addProject(PROJECT_NAME, PROJECT_KEY, ADMIN.getUsername());
        }
    }
}
