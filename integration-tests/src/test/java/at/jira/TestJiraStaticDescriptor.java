package at.jira;

import java.rmi.RemoteException;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import com.google.common.base.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.util.ExternalAddonInstaller;
import it.jira.JiraWebDriverTestBase;

@Category (OnDemandAcceptanceTest.class)
public class TestJiraStaticDescriptor extends JiraWebDriverTestBase
{
    private static final String WEB_ITEM_ID = "com.atlassian.connect.acceptance.test__opsbar-test-web-item";

    protected static final ExternalAddonInstaller externalAddonInstaller = new ExternalAddonInstaller(
            product.getProductInstance().getBaseUrl(), testUserFactory.admin());

    private static final Logger log = LoggerFactory.getLogger(TestJiraStaticDescriptor.class);

    @Before
    public void installAddon() throws Exception
    {
        log.info("Installing add-on in preparation for running a test in " + getClass().getName());
        externalAddonInstaller.install();
    }

    @Test
    public void testAcActionWebItemIsPresent() throws RemoteException
    {
        IssueCreateResponse issue = product.backdoor().issues().createIssue(project.getKey(), "Atlassian Connect Web Panel Test Issue");

        login(testUserFactory.basicUser());
        product.goToViewIssue(issue.key);

        connectPageOperations.findWebItem(WEB_ITEM_ID, Optional.<String>absent());
    }

    @After
    public void uninstallAddon() throws Exception
    {
        log.info("Cleaning up after running a test in " + getClass().getName());
        externalAddonInstaller.uninstall();
    }
}
