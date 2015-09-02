package at.jira;

import java.rmi.RemoteException;

import at.marketplace.MarketplaceAddonConstants;
import com.atlassian.connect.acceptance.test.AtlassianConnectMarketplaceInstaller;
import com.atlassian.connect.acceptance.test.TestUser;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import com.google.common.base.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.jira.JiraWebDriverTestBase;

import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;

@Category (OnDemandAcceptanceTest.class)
public class TestJiraStaticDescriptor extends JiraWebDriverTestBase
{
    private static final Logger log = LoggerFactory.getLogger(TestJiraStaticDescriptor.class);

    private static final AtlassianConnectMarketplaceInstaller marketplaceInstaller = new AtlassianConnectMarketplaceInstaller(
            MarketplaceAddonConstants.ADD_ON_REPRESENTATION,
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

        connectPageOperations.findWebItem(LINK_TEXT, MarketplaceAddonConstants.WEB_ITEM_TEXT, Optional.<String>absent());
    }

    @After
    public void uninstallAddon() throws Exception
    {
        log.info("Cleaning up after running a test in " + getClass().getName());

        marketplaceInstaller.uninstall();
    }
}
