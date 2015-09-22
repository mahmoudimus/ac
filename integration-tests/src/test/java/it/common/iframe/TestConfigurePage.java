package it.common.iframe;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.ConfigurePageModuleMeta;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.pageobjects.PluginManagerPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.upm.pageobjects.PluginManager;
import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Test of addon configure page in Confluence
 */
public class TestConfigurePage extends AbstractPageTestBase
{
    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        startConnectAddOn("configurePage", new ConfigurePageModuleMeta());
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
                .addModuleMeta(new ConfigurePageModuleMeta())
                .addRoute("/page", ConnectAppServlets.helloWorldServlet())
                .addScope(ScopeName.READ)
                .start();

        try
        {
            login(testUserFactory.admin());
            final PluginManagerPage upm = product.visit(PluginManagerPage.class);

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
    }
}
