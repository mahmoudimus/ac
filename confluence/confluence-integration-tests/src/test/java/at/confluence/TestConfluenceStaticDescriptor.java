package at.confluence;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.product.ConfluenceTestedProductAccessor;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectPageOperations;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import com.google.common.base.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.plugin.connect.test.common.util.TestUser;

import static com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;
import static com.atlassian.plugin.connect.test.product.ConfluenceTestedProductAccessor.toConfluenceUser;

@Category (OnDemandAcceptanceTest.class)
public class TestConfluenceStaticDescriptor
{
    private static final String WEB_ITEM_TEXT = "AC Action";
    private static final Logger log = LoggerFactory.getLogger(TestConfluenceStaticDescriptor.class);
    private static final TestUser ADMIN = new TestUser("admin");

    private final ConfluenceTestedProduct product = new ConfluenceTestedProductAccessor().getConfluenceProduct();
    private final ExternalAddonInstaller externalAddonInstaller =
            new ExternalAddonInstaller(product.getProductInstance().getBaseUrl(), ADMIN);
    private final ConnectPageOperations connectPageOperations = new ConnectPageOperations(
            product.getPageBinder(), product.getTester().getDriver());

    @Before
    public void installAddon() throws Exception
    {
        log.info("Installing add-on in preparation for running a test in " + getClass().getName());
        externalAddonInstaller.install();
    }

    @Test
    public void testAcDashboardWebItemIsPresent()
    {
        product.login(toConfluenceUser(ADMIN), DashboardPage.class);
        connectPageOperations.findWebItem(LINK_TEXT, WEB_ITEM_TEXT, Optional.<String>absent());
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
        externalAddonInstaller.uninstall();
    }
}
