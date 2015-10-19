package it.common.iframe;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.pageobjects.PluginManagerPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.testutils.annotations.Retry;
import com.atlassian.upm.pageobjects.PluginManager;
import it.common.RetryTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Test of addon configure page in Confluence
 */
@Retry(maxAttempts= RetryTestBase.MAX_ATTEMPTS)
public class TestConfigurePage extends AbstractPageTestBase
{
    private static final Logger logger = LoggerFactory.getLogger(TestConfigurePage.class);

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        logger.debug("TestConfigurePageDebug");
        logger.info("TestConfigurePageInfo");
        logger.error("TestConfigurePageError");
        startConnectAddOn("configurePage");
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        runCanClickOnPageLinkAndSeeAddonContents(PluginManager.class, Option.some("Configure"), testUserFactory.admin());
    }

    @Override
    protected <T extends Page> void revealLinkIfNecessary(T page)
    {
        // hmmm not pretty
        ((PluginManager)page).expandPluginRow(pluginKey);
    }

    @Test
    public void testConfigurePage() throws Exception
    {
        ConnectRunner anotherPlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addInstallLifecycle()
                .addRoute(ConnectRunner.INSTALLED_PATH, new InstallHandlerServlet())
                .addModule("configurePage", newPageBean()
                        .withName(new I18nProperty("Page", null))
                        .withKey("page")
                        .withLocation("")
                        .withUrl("/page")
                        .build())
                .addRoute("/page", ConnectAppServlets.helloWorldServlet())
                .addScope(ScopeName.READ)
                .start();

        try
        {
            final PluginManagerPage upm = loginAndVisit(testUserFactory.admin(), PluginManagerPage.class);

            upm.clickConfigurePluginButton(anotherPlugin.getAddon().getKey(), "page");
            product.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, anotherPlugin.getAddon().getKey(), "page", true); // will throw if it fails to load
        }
        finally
        {
            anotherPlugin.stopAndUninstall();
        }
    }

    @Test
    public void pageIsNotAccessibleWithFalseCondition()
    {
        runner.setToggleableConditionShouldDisplay(false);

        login(testUserFactory.basicUser());

        // note we don't check that the configure link isn't displayed due to AC-973

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class,
                pluginKey, MY_AWESOME_PAGE_KEY);
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString(MY_AWESOME_PAGE));

        logger.error("MAX_ATTEMPTS: " + RetryTestBase.MAX_ATTEMPTS + "\n");
        boolean pass = ( ( Math.random() * 10 ) > 8 );
        logger.error("flakyTest() pass: " + pass + "\n");
        if (!pass) assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("FailFailFail"));
    }
}
