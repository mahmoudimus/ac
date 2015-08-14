package it.common.upm;

import com.atlassian.fugue.Option;
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

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;

/**
 * Test of addon configure page in Confluence
 */
public class TestPostInstallPage extends AbstractPageTestBase
{
    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        startConnectAddOn("postInstallPage");
    }

//    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        runCanClickOnPageLinkAndSeeAddonContents(PluginManager.class, Option.some("Get started"), testUserFactory.admin());
    }

//    @Test
    public void testPostInstallPage() throws Exception
    {
        ConnectRunner anotherPlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addInstallLifecycle()
                .addRoute(ConnectRunner.INSTALLED_PATH, new InstallHandlerServlet())
                .addModule("postInstallPage", newPageBean()
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
            //URI configurePageURI = upm.getPlugin(anotherPlugin.getAddon().getKey()).openPluginDetails().getPostInstallLink().getHref();
            //product.getTester().getDriver().navigate().to(configurePageURI.toURL());
            product.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, anotherPlugin.getAddon().getKey(), "page", true); // will throw if it fails to load
        }
        finally
        {
            anotherPlugin.stopAndUninstall();
        }
    }
}
