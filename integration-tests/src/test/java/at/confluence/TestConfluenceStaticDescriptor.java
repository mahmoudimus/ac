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
    public static final String WEB_ITEM_ID = "com.atlassian.connect.acceptance.test__browse-test-web-item";

    @BeforeClass
    public static void installAddon() throws Exception
    {
        externalAddonInstaller.install();
    }

    @AfterClass
    public static void uninstallAddon() throws Exception
    {
        externalAddonInstaller.uninstall();
    }

    @Test
    public void testAcDashboardWebItemIsPresent()
    {
        loginAndVisit(TestUser.ADMIN, DashboardPage.class);
        connectPageOperations.findWebItem(WEB_ITEM_ID, Optional.<String>absent());
    }
}
