package at.jira;

import java.util.List;

import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.Project;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import at.util.ExternalAddonInstaller;
import it.jira.JiraWebDriverTestBase;

import static com.google.common.collect.Iterables.any;
import static it.util.TestUser.ADMIN;
import static it.util.TestUser.SYSADMIN;

@Category (OnDemandAcceptanceTest.class)
public class TestJiraStaticDescriptor extends JiraWebDriverTestBase
{
    // TODO: ACDEV-1787 - update this url to point to the marketplace-hosted atlassian-connect.json
    private static final String ADDON_URL = "https://bitbucket.org/jimihazelwood/ac-acceptance-test-addons/raw/master/jira/atlassian-connect.json";
    private static final String PROJECT_KEY = "ACTEST";
    private static final String PROJECT_NAME = "Atlassian Connect Test";
    private static final String APP_KEY = "com.atlassian.connect.at.jira";
    private static final ExternalAddonInstaller installer = new ExternalAddonInstaller(
                product.getProductInstance().getBaseUrl(), SYSADMIN);
    private static final String WEB_ITEM_ID = "com.atlassian.connect.at.jira__opsbar-test-web-item";

    @BeforeClass
    public static void installAddon() throws Exception
    {
        installer.install(ADDON_URL);
    }

    @AfterClass
    public static void uninstallAddon() throws Exception
    {
        installer.uninstall(APP_KEY);
    }

    @Test
    @LoginAs(admin = true)
    public void testAcActionWebItemIsPresent()
    {
        createConnectProject();
        IssueCreateResponse response = getProduct().backdoor().issues()
                .createIssue(PROJECT_KEY, "Atlassian Connect Web Panel Test Issue");

        getProduct().goToViewIssue(response.key());
        connectPageOperations.findWebItem(WEB_ITEM_ID, Optional.<String>absent());
    }

    private void createConnectProject()
    {
        List<Project> projects = getProduct().backdoor().project().getProjects();
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
            getProduct().backdoor().project().addProject(PROJECT_NAME, PROJECT_KEY, ADMIN.getUsername());
        }
    }
}
