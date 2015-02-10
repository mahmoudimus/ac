package at.confluence;

import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import com.google.common.base.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import it.util.TestUser;

@Category (OnDemandAcceptanceTest.class)
public class TestConfluenceStaticDescriptor extends ConfluenceAcceptanceTestBase
{
    // TODO: ACDEV-1787 - update this url to point to the marketplace-hosted atlassian-connect.json
    private static final String ADDON_URL = "https://bitbucket.org/jimihazelwood/ac-acceptance-test-addons/raw/master/confluence/atlassian-connect.json";
    private static final String APP_KEY = "com.atlassian.connect.at.confluence";
    public static final String WEB_ITEM_ID = "com.atlassian.connect.at.confluence__browse-test-web-item";

    @BeforeClass
    public static void installAddon() throws Exception
    {
        externalAddonInstaller.install(ADDON_URL);
    }

    @AfterClass
    public static void uninstallAddon() throws Exception
    {
        externalAddonInstaller.uninstall(APP_KEY);
    }

    @Test
    public void testAcDashboardWebItemIsPresent()
    {
        loginAndVisit(TestUser.BARNEY, DashboardPage.class);
        connectPageOperations.findWebItem(WEB_ITEM_ID, Optional.<String>absent());
    }
}
