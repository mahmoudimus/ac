package at.jira;

import com.atlassian.plugin.connect.test.common.at.AcceptanceTestHelper;
import com.atlassian.test.categories.OnDemandAcceptanceTest;
import com.atlassian.testutils.annotations.Retry;
import it.jira.JiraWebDriverTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.Optional;

import static com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;
import static com.atlassian.test.ondemand.data.JiraData.Projects.EntityLinkedProject.Issues.ISSUE_WITH_WIKI_LINK;

@Category(OnDemandAcceptanceTest.class)
@Retry(maxAttempts = 1)
@Ignore
public class TestJiraStaticDescriptor extends JiraWebDriverTestBase {
    private static final String WEB_ITEM_TEXT = "AC Action";
    private static final String ADDON_DESCRIPTOR_URL = "https://bitbucket.org/atlassianlabs/ac-acceptance-test-addon/raw/addon-0001/atlassian-connect.json";

    private AcceptanceTestHelper acceptanceTestHelper;

    private static final Logger log = LoggerFactory.getLogger(TestJiraStaticDescriptor.class);

    @Before
    public void installAddon() throws Exception {
        acceptanceTestHelper = new AcceptanceTestHelper(testUserFactory.admin(), ADDON_DESCRIPTOR_URL, product);
        log.info("Installing add-on in preparation for running a test in " + getClass().getName());
        acceptanceTestHelper.installAddon();
    }

    @Test
    public void testAcActionWebItemIsPresent() throws RemoteException {
        login(testUserFactory.basicUser());
        product.goToViewIssue(ISSUE_WITH_WIKI_LINK.key);

        connectPageOperations.findWebItem(LINK_TEXT, WEB_ITEM_TEXT, Optional.<String>empty());
    }

    @After
    public void uninstallAddon() throws Exception {
        log.info("Cleaning up after running a test in " + getClass().getName());
        acceptanceTestHelper.uninstallAddon();
    }
}
