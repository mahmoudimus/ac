package it.common.upm;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.upm.pageobjects.InstalledPluginDetails;
import com.atlassian.upm.pageobjects.Link;
import com.atlassian.upm.pageobjects.PluginManager;
import it.common.iframe.AbstractPageTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import org.junit.Test;

import java.net.URI;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Test of addon configure page in Confluence
 */
public abstract class AbstractUpmPageTest extends AbstractPageTestBase
{
    @Override
    protected <T extends Page> void revealLinkIfNecessary(T page)
    {
        ((PluginManager) page).expandPluginRow(pluginKey);
    }

    @Test
    public void testPage() throws Exception
    {
        ConnectRunner anotherPlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addInstallLifecycle()
                .addRoute(ConnectRunner.INSTALLED_PATH, new InstallHandlerServlet())
                .addModule(getModuleType(), newPageBean()
                        .withName(new I18nProperty("Page", null))
                        .withKey("page")
                        .withLocation("")
                        .withUrl("/page")
                        .build())
                .addRoute("/page", ConnectAppServlets.helloWorldServlet())
                .addScope(ScopeName.READ)
                .enableLicensing()
                .start();
        try
        {
            login(testUserFactory.admin());
            final PluginManager upm = product.visit(PluginManager.class);
            InstalledPluginDetails installedPluginDetails = upm.getPlugin(anotherPlugin.getAddon().getKey()).openPluginDetails();
            Link configureLink = getLink(installedPluginDetails);
            URI configurePageURI = configureLink.getHref();
            product.getTester().getDriver().navigate().to(configurePageURI.toURL());
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
    
    abstract String getModuleType();
    
    abstract Link getLink(InstalledPluginDetails installedPluginDetails);
}
