package it.capabilities.confluence;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceAdminPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceAdminHomePage;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean.newPageBean;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test of general page in Confluence
 */
public class TestAdminPage extends ConfluenceWebDriverTestBase
{
    private static final String PLUGIN_KEY = "my-plugin";

    private static final String PAGE_NAME = "My Admin Page";
    private static final String GENERATED_PAGE_KEY = "my-admin-page";

    private static ConnectCapabilitiesRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addCapability(
                        "adminPages",
                        newPageBean()
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withUrl("/pg")
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.helloWorldServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws Exception
    {
        loginAsAdmin();
        product.visit(FixedConfluenceAdminHomePage.class);

        ConfluenceAdminPage viewProjectPage = product.getPageBinder().bind(ConfluenceAdminPage.class, GENERATED_PAGE_KEY);

        assertThat(viewProjectPage.isRemotePluginLinkPresent(), is(true));

        URI url = new URI(viewProjectPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/confluence/plugins/servlet/ac/my-plugin/pg"));

        // TODO Admin page web-item location has incorrect text ("OSGi")

        RemotePluginTestPage addonContentsPage = viewProjectPage.clickRemotePluginLink();
        assertEquals("Hello world", addonContentsPage.getValueBySelector("#hello-world-message"));
    }

}
