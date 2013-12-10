package it.capabilities.confluence;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceBasePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AbstractPageTst extends ConfluenceWebDriverTestBase
{
    private static final String PLUGIN_KEY = "my-plugin";

    private static ConnectRunner remotePlugin;

    protected static void startConnectAddOn(String fieldName) throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addModule(
                        fieldName,
                        newPageBean()
                                .withName(new I18nProperty("My Awesome Page", null))
                                .withUrl("/pg")
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.apRequestServlet())
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

    protected <T extends ConfluenceBasePage> void runCanClickOnPageLinkAndSeeAddonContents(Class<T> pageClass)
            throws MalformedURLException, URISyntaxException
    {
        loginAsAdmin();

        T containerPage = product.visit(pageClass);
        LinkedRemoteContent addonPage = containerPage.findConnectPage(LINK_TEXT, "My Awesome Page",
                Option.<String>none(), "my-awesome-page");
        RemotePluginEmbeddedTestPage addonContentPage = addonPage.click();
        assertThat(addonContentPage.isLoaded(), equalTo(true));
        assertThat(addonContentPage.getMessage(), equalTo("Success"));
    }


}
