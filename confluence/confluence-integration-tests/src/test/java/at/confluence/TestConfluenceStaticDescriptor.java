package at.confluence;

import com.atlassian.confluence.it.rpc.ConfluenceRpc;
import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.plugin.connect.test.common.at.AcceptanceTestHelper;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectPageOperations;
import com.atlassian.test.categories.OnDemandAcceptanceTest;
import com.atlassian.webdriver.testing.rule.LogPageSourceRule;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.atlassian.confluence.it.User.SYS_ADMIN;
import static com.atlassian.confluence.it.rpc.ConfluenceRpc.Version.V2_WITH_WIKI_MARKUP;
import static com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;
import static com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor.toConfluenceUser;

@Category(OnDemandAcceptanceTest.class)
@Ignore
public class TestConfluenceStaticDescriptor extends ConfluenceAcceptanceTestBase {
    private static final String WEB_ITEM_TEXT = "AC Action";
    private static final Logger log = LoggerFactory.getLogger(TestConfluenceStaticDescriptor.class);
    public static final String DASHBOARD_ONBOARDING_DISABLED = "dashboard.onboarding.disabled";
    private static final String ADDON_DESCRIPTOR_URL = "https://bitbucket.org/atlassianlabs/ac-acceptance-test-addon/raw/addon-0001/atlassian-connect.json";

    private AcceptanceTestHelper acceptanceTestHelper;

    private final ConnectPageOperations connectPageOperations = new ConnectPageOperations(
            product.getPageBinder(), product.getTester().getDriver());
    protected final ConfluenceRpc rpc = ConfluenceRpc.newInstance(product.getProductInstance().getBaseUrl(), V2_WITH_WIKI_MARKUP);

    @Rule
    public WebDriverScreenshotRule screenshotRule = new WebDriverScreenshotRule();

    @Rule
    public LogPageSourceRule pageSourceRule = new LogPageSourceRule();
    private boolean dashboardOnboardingEnabled;

    @Before
    public void installAddon() throws Exception {
        acceptanceTestHelper = new AcceptanceTestHelper(ADMIN, ADDON_DESCRIPTOR_URL, product);
        log.info("Installing add-on in preparation for running a test in " + getClass().getName());
        acceptanceTestHelper.installAddon();
    }

    @Before
    public void logOut() {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    @Before
    public void disableOnboarding() {
        rpc.logIn(SYS_ADMIN);
        dashboardOnboardingEnabled = !rpc.darkFeatures.isSiteFeatureEnabled(DASHBOARD_ONBOARDING_DISABLED);
        if (dashboardOnboardingEnabled) {
            rpc.darkFeatures.enableSiteFeature(DASHBOARD_ONBOARDING_DISABLED);
        }
    }

    @Test
    public void testAcDashboardWebItemIsPresent() {
        product.login(toConfluenceUser(ADMIN), DashboardPage.class);
        connectPageOperations.findWebItem(LINK_TEXT, WEB_ITEM_TEXT, Optional.<String>empty());
    }

    @After
    public void restoreOnboarding() {
        rpc.logIn(SYS_ADMIN);
        if (dashboardOnboardingEnabled) {
            rpc.darkFeatures.disableSiteFeature(DASHBOARD_ONBOARDING_DISABLED);
        }
    }

    @After
    public void uninstallAddon() throws Exception {
        log.info("Cleaning up after running a test in " + getClass().getName());
        acceptanceTestHelper.uninstallAddon();
    }
}
