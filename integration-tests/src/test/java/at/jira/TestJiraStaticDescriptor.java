package at.jira;

import java.rmi.RemoteException;

import com.atlassian.connect.acceptance.test.AtlassianConnectMarketplaceInstaller;
import com.atlassian.connect.acceptance.test.ConnectAddonRepresentation;
import com.atlassian.connect.acceptance.test.TestUser;
import com.atlassian.connect.acceptance.test.marketplace.MarketplacePublisher;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.plugin.connect.test.client.AtlassianConnectRestClient;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import com.google.common.base.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.marketplace.ExternalAddonInstaller;
import it.jira.JiraWebDriverTestBase;

import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;

@Category (OnDemandAcceptanceTest.class)
public class TestJiraStaticDescriptor extends JiraWebDriverTestBase
{
    private static final Logger log = LoggerFactory.getLogger(TestJiraStaticDescriptor.class);
    private static final long ATLASSIAN_LABS_ID = 33202;
    private static final String TEST_ADDON_VERSION = "0001";
    private static final String WEB_ITEM_TEXT = "AC Action";

    private static final AtlassianConnectMarketplaceInstaller marketplaceInstaller = new AtlassianConnectMarketplaceInstaller(
            ConnectAddonRepresentation.builder()
                    .withDescriptorUrl("https://bitbucket.org/atlassianlabs/ac-acceptance-test-addon/raw/addon-" + TEST_ADDON_VERSION + "/atlassian-connect.json")
                    .withLogo("https://bitbucket.org/atlassianlabs/ac-acceptance-test-addon/raw/addon-" + TEST_ADDON_VERSION + "/simple-logo.png")
                    .withKey("com.atlassian.connect.acceptance.test.addon." + TEST_ADDON_VERSION)
                    .withName("Connect Test Addon v" + TEST_ADDON_VERSION) // Must be < 40 characters
                    .withVendorId(ATLASSIAN_LABS_ID)
                    .build(),
            new TestUser(testUserFactory.admin().getUsername()),
            product.getProductInstance().getBaseUrl()
    );


    @Before
    public void installAddon() throws Exception
    {
        marketplaceInstaller.installAddon();

        log.info("Installing add-on in preparation for running a test in " + getClass().getName());
    }

    @Test
    public void testAcActionWebItemIsPresent() throws RemoteException
    {
        IssueCreateResponse issue = product.backdoor().issues().createIssue(project.getKey(), "Atlassian Connect Web Panel Test Issue");

        login(testUserFactory.basicUser());
        product.goToViewIssue(issue.key);

        connectPageOperations.findWebItem(LINK_TEXT, WEB_ITEM_TEXT, Optional.<String>absent());
    }

    @After
    public void uninstallAddon() throws Exception
    {
        log.info("Cleaning up after running a test in " + getClass().getName());

        marketplaceInstaller.uninstall();
    }
}
