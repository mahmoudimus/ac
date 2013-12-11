package it.capabilities.confluence;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceAddonConfigurePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.upm.pageobjects.PluginRow;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConfigurePageModuleBean.newConfigurePageBean;
import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of addon configure page in Confluence
 */
public class TestConfigurePage extends ConfluenceWebDriverTestBase
{

    private static final String MY_AWESOME_PAGE = "My Awesome Page";
    private static final String MY_AWESOME_PAGE_KEY = "my-awesome-page";

    private static final String PLUGIN_KEY = "my-plugin";
    private static final String URL = "/" + MY_AWESOME_PAGE_KEY;


    private static ConnectRunner remotePlugin;

    protected static void startConnectAddOn(String fieldName) throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addModule(
                        fieldName,
                        newConfigurePageBean()
                                .withName(new I18nProperty(MY_AWESOME_PAGE, null))
                                .withUrl(URL)
                                .withWeight(1234)
                                .build())
                .addRoute(URL, ConnectAppServlets.apRequestServlet())
                .start();
    }
    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        startConnectAddOn("configurePages");
    }


    /*
     * TODO: Is there any way to sensibly extend AbstractPageTst or is that square peg in a round hole?
     */



    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        loginAsAdmin();

        ConfluenceAddonConfigurePage containerPage = product.visit(ConfluenceAddonConfigurePage.class);
        PluginRow pluginRow = containerPage.expandPluginRow(PLUGIN_KEY);

        LinkedRemoteContent addonPage = containerPage.getConnectPageHelper().findConnectPage(LINK_TEXT, "Configure",
                Option.<String>none(), MY_AWESOME_PAGE_KEY);
        RemotePluginEmbeddedTestPage addonContentPage = addonPage.click();
        assertThat(addonContentPage.isLoaded(), equalTo(true));
        assertThat(addonContentPage.getMessage(), equalTo("Success"));
    }


}
