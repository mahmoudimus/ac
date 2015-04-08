package at.jira;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import com.google.common.base.Optional;

import it.util.ConnectTestUserFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.util.ExternalAddonInstaller;
import it.jira.JiraWebDriverTestBase;
import it.util.TestUser;

@Category (OnDemandAcceptanceTest.class)
public class TestJiraStaticDescriptor extends JiraWebDriverTestBase
{
    private static final String WEB_ITEM_ID = "com.atlassian.connect.acceptance.test__opsbar-test-web-item";

    protected static final ExternalAddonInstaller externalAddonInstaller = new ExternalAddonInstaller(
            product.getProductInstance().getBaseUrl(), ConnectTestUserFactory.admin(product));

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
        TestUser user = ConnectTestUserFactory.basicUser(product);
        IssueCreateResponse response = product.backdoor().issues().loginAs(user.getUsername())
                .createIssue(project.getKey(), "Atlassian Connect Web Panel Test Issue");

        product.quickLogin(user.getUsername(), user.getPassword());
        product.goToViewIssue(response.key());

        connectPageOperations.findWebItem(WEB_ITEM_ID, Optional.<String>absent());
    }

    @After
    public void uninstallAddon() throws Exception
    {
        log.info("Cleaning up after " + getClass().getName());
        externalAddonInstaller.uninstall();
    }
}
