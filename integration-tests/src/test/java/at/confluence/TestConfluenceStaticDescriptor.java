package at.confluence;

import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import com.google.common.base.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.util.ExternalAddonInstaller;
import it.confluence.ConfluenceWebDriverTestBase;

@Category (OnDemandAcceptanceTest.class)
public class TestConfluenceStaticDescriptor extends ConfluenceWebDriverTestBase
{
    public static final String WEB_ITEM_ID = "com.atlassian.connect.acceptance.test__browse-test-web-item";
    private static final Logger log = LoggerFactory.getLogger(TestConfluenceStaticDescriptor.class);
    private static final ExternalAddonInstaller externalAddonInstaller =
            new ExternalAddonInstaller(product.getProductInstance().getBaseUrl(), testUserFactory.admin());

    @Before
    public void installAddon() throws Exception
    {
        log.info("Installing add-on in preparation for running a test in " + getClass().getName());
        externalAddonInstaller.install();
    }

    @Test
    public void testAcDashboardWebItemIsPresent()
    {
        loginAndVisit(testUserFactory.basicUser(), DashboardPage.class);
        connectPageOperations.findWebItem(WEB_ITEM_ID, Optional.<String>absent());
    }

    @After
    public void uninstallAddon() throws Exception
    {
        log.info("Cleaning up after running a test in " + getClass().getName());
        externalAddonInstaller.uninstall();
    }
}
