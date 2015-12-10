package at.jira;

import java.rmi.RemoteException;
import java.util.Optional;

import com.atlassian.test.categories.OnDemandAcceptanceTest;
import com.atlassian.testutils.annotations.Retry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.marketplace.ExternalAddonInstaller;
import it.jira.JiraWebDriverTestBase;

import static com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;
import static com.atlassian.test.ondemand.data.JiraData.Projects.EntityLinkedProject.Issues.ISSUE_WITH_WIKI_LINK;

@Category (OnDemandAcceptanceTest.class)
@Retry (maxAttempts=1)
public class TestJiraStaticDescriptor extends JiraWebDriverTestBase
{
    private static final String WEB_ITEM_TEXT = "AC Action";

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
        login(testUserFactory.basicUser());
        product.goToViewIssue(ISSUE_WITH_WIKI_LINK.key);

        connectPageOperations.findWebItem(LINK_TEXT, WEB_ITEM_TEXT, Optional.<String>empty());
    }

    @After
    public void uninstallAddon() throws Exception
    {
        log.info("Cleaning up after running a test in " + getClass().getName());
        externalAddonInstaller.uninstall();
    }
}
