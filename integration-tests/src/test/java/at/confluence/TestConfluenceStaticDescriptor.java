package at.confluence;

import at.marketplace.MarketplaceAddonConstants;
import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.connect.acceptance.test.AtlassianConnectMarketplaceInstaller;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider;
import com.atlassian.test.categories.OnDemandAcceptanceTest;
import com.google.common.base.Optional;
import it.util.TestUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;

@Category (OnDemandAcceptanceTest.class)
public class TestConfluenceStaticDescriptor
{
    private static final Logger log = LoggerFactory.getLogger(TestConfluenceStaticDescriptor.class);
    private static final TestUser ADMIN = new TestUser("admin");

    private static final ConfluenceTestedProduct product = TestedProductProvider.getConfluenceTestedProduct();
    private final ConnectPageOperations connectPageOperations = new ConnectPageOperations(
            product.getPageBinder(), product.getTester().getDriver());

    private static final AtlassianConnectMarketplaceInstaller marketplaceInstaller = new AtlassianConnectMarketplaceInstaller(
            MarketplaceAddonConstants.ADD_ON_REPRESENTATION,
            new com.atlassian.connect.acceptance.test.TestUser(ADMIN.getUsername()),
            product.getProductInstance().getBaseUrl()
    );

    @Before
    public void installAddon() throws Exception
    {
        log.info("Installing add-on in preparation for running a test in " + getClass().getName());
        marketplaceInstaller.installAddon();
    }

    @Test
    public void testAcDashboardWebItemIsPresent()
    {
        product.login(ADMIN.confUser(), DashboardPage.class);
        connectPageOperations.findWebItem(LINK_TEXT, MarketplaceAddonConstants.WEB_ITEM_TEXT, Optional.<String>absent());
    }

    @Before
    public void logOut()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    @After
    public void uninstallAddon() throws Exception
    {
        log.info("Cleaning up after running a test in " + getClass().getName());
        marketplaceInstaller.uninstall();
    }
}
