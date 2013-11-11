package it.capabilities.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceGeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;
import java.net.URI;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean.newPageBean;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test of general page in Confluence
 */
public class TestGeneralPage extends ConfluenceWebDriverTestBase
{
    private static final String PLUGIN_KEY = "my-plugin";
    private static final String SPACE = "ds";

    private static ConnectCapabilitiesRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addCapability(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty("My Awesome Page", null))
                                .withLocation("system.browse")
                                .withUrl("/pg?page_id=${page.id}")
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
            remotePlugin.stop();
        }
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws Exception
    {
        loginAsAdmin();

        ConfluenceViewPage confluenceViewPage = createAndVisitViewPage();

        ConfluenceGeneralPage viewProjectPage = product.getPageBinder().bind(ConfluenceGeneralPage.class, "my-awesome-page", "My Awesome Page", true);

        assertThat(viewProjectPage.isRemotePluginLinkPresent(), is(true));

        URI url = new URI(viewProjectPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/confluence/plugins/servlet/ac/my-plugin/pg"));

        assertThat(URLEncodedUtils.parse(url, "UTF-8"),
                containsInAnyOrder(
                        (NameValuePair) new BasicNameValuePair("page_id", confluenceViewPage.getPageId())
                )
        );

        RemotePluginTestPage addonContentsPage = viewProjectPage.clickRemotePluginLink();
        assertThat(addonContentsPage.getMessage(), is("Success"));
    }

    private ConfluenceViewPage createAndVisitViewPage() throws Exception
    {
        return createAndVisitPage(ConfluenceViewPage.class);
    }


    private <P extends Page> P createAndVisitPage(Class<P> pageClass) throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = createPage();
        return product.visit(pageClass, pageData.getId());
    }

    private ConfluenceOps.ConfluencePageData createPage() throws MalformedURLException, XmlRpcFault
    {
        return confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), SPACE, "A test page", "some page content");
    }

}
