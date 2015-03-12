package at.confluence;

import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import com.google.common.base.Optional;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.util.TestUser;

@Category (OnDemandAcceptanceTest.class)
public class TestConfluenceStaticDescriptor extends ConfluenceAcceptanceTestBase
{
    public static final String WEB_ITEM_ID = "com.atlassian.connect.acceptance.test__browse-test-web-item";
    private static final Logger log = LoggerFactory.getLogger(TestConfluenceStaticDescriptor.class);

    @Test
    public void testAcDashboardWebItemIsPresent()
    {
        log.info("Installing add-on in preparation for running " + TestConfluenceStaticDescriptor.class.getName());
        externalAddonInstaller.install();
        loginAndVisit(TestUser.ADMIN, DashboardPage.class);
        connectPageOperations.findWebItem(WEB_ITEM_ID, Optional.<String>absent());
        log.info("Cleaning up after " + TestConfluenceStaticDescriptor.class.getName());
        externalAddonInstaller.uninstall();
    }
}
