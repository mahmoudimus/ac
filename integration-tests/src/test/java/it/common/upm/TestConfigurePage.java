package it.common.upm;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.upm.pageobjects.PluginManager;
import it.common.iframe.AbstractPageTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;

/**
 * Test of addon configure page in Confluence
 */
public class TestConfigurePage extends AbstractPageTestBase
{
    @Inject
    PageBinder pageBinder;
    
    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        startConnectAddOn("configurePage");
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        runCanClickOnPageLinkAndSeeAddonContents(PluginManager.class, Option.some("Configure"), testUserFactory.admin());
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
            login(testUserFactory.admin());
            final PluginManager upm = product.visit(PluginManager.class);
            URI configurePageURI = upm.getPlugin(anotherPlugin.getAddon().getKey()).openPluginDetails().getConfigureLink().getHref();
            product.getTester().getDriver().navigate().to(configurePageURI.toURL());
            product.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, anotherPlugin.getAddon().getKey(), "page", true); // will throw if it fails to load
        }
        finally
        {
            anotherPlugin.stopAndUninstall();
        }
    }
}
