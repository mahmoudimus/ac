package it.capabilities.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import it.confluence.ConfluenceWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;

import static com.atlassian.fugue.Option.some;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class TestConfluenceWebPanel extends ConfluenceWebDriverTestBase
{
    private static final String IFRAME_URL = "http://www.example.com";
    private static final String SPACE = "ds";

    private static ConnectCapabilitiesRunner remotePlugin;
    private static WebPanelCapabilityBean webPanelCapability;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        webPanelCapability = WebPanelCapabilityBean.newWebPanelBean()
                .withName(new I18nProperty("Connect Panel", "connect-panel"))
                .withLocation("atl.editor")
                .withUrl(IFRAME_URL)
                .withLayout(new WebPanelLayout("100%", "200px"))
                .withWeight(1)
                .build();

        remotePlugin = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addCapability(webPanelCapability)
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stop();
        }
    }

    @Before
    public void beforeEachTest()
    {
        loginAsAdmin();
    }

    @Test
    public void webPanelExistsOnEditPage() throws Exception
    {
        ConfluenceEditPage editPage = visitEditPage();
        RemoteWebPanel webPanel = editPage.findWebPanel(webPanelCapability.getKey());
        assertThat(webPanel, is(not(nullValue())));
    }

    @Test
    public void iFrameUrlIsCorrectOnEditPage() throws Exception
    {
        ConfluenceEditPage editPage = visitEditPage();
        RemoteWebPanel webPanel = editPage.findWebPanel(webPanelCapability.getKey());
        assertThat(webPanel.getIFrameSourceUrl(), startsWith(IFRAME_URL)); // will end with the plugin's displayUrl and auth parameters
    }

    @Test
    public void webPanelExistsOnViewPage() throws Exception
    {
        ConfluenceViewPage viewPage = visitViewPage();
        RemoteWebPanel webPanel = viewPage.findWebPanel(webPanelCapability.getKey());
        assertThat(webPanel, is(not(nullValue())));
    }

    @Test
    public void iFrameUrlIsCorrectOnViewPage() throws Exception
    {
        ConfluenceViewPage viewPage = visitViewPage();
        RemoteWebPanel webPanel = viewPage.findWebPanel(webPanelCapability.getKey());
        assertThat(webPanel.getIFrameSourceUrl(), startsWith(IFRAME_URL)); // will end with the plugin's displayUrl and auth parameters
    }

    private ConfluenceViewPage visitViewPage() throws Exception
    {
        return visitPage(ConfluenceViewPage.class);
    }

    private ConfluenceEditPage visitEditPage() throws Exception
    {
        return visitPage(ConfluenceEditPage.class);
    }

    private <P extends Page> P visitPage(Class<P> pageClass) throws Exception
    {
        final ConfluenceOps.ConfluencePageData pageData = createPage();
        return product.visit(pageClass, pageData.getId());
    }

    private ConfluenceOps.ConfluencePageData createPage() throws MalformedURLException, XmlRpcFault
    {
        return confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), SPACE, "Page with webpanel", "some page content");
    }

}
