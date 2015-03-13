package at.jira;

import java.util.List;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.Project;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Iterables.any;
import static it.util.TestUser.ADMIN;

@Category (OnDemandAcceptanceTest.class)
public class TestJiraStaticDescriptor extends JiraAcceptanceTestBase
{
    private static final String PROJECT_KEY = "ACTEST";
    private static final String PROJECT_NAME = "Atlassian Connect Test";
    private static final String WEB_ITEM_ID = "com.atlassian.connect.acceptance.test__opsbar-test-web-item";

    private static final Logger log = LoggerFactory.getLogger(TestJiraStaticDescriptor.class);

    @Before
    public void installAddon() throws Exception
    {
        log.info("Installing add-on in preparation for running " + getClass().getName());
        externalAddonInstaller.install();
    }

    @Test
    public void testAcActionWebItemIsPresent()
    {
        createConnectProject();
        IssueCreateResponse response = product.backdoor().issues().loginAs("admin")
                .createIssue(PROJECT_KEY, "Atlassian Connect Web Panel Test Issue");

        product.quickLogin(ADMIN.getUsername(), ADMIN.getPassword());
        product.goToViewIssue(response.key());

        connectPageOperations.findWebItem(WEB_ITEM_ID, Optional.<String>absent());
    }

    @After
    public void uninstallAddon() throws Exception
    {
        log.info("Cleaning up after " + getClass().getName());
        externalAddonInstaller.uninstall();
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
