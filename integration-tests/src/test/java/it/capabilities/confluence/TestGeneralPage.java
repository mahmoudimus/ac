package it.capabilities.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceGeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
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
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageModuleBean.newPageBean;
import static com.google.common.base.Charsets.UTF_8;
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
    private static final String ADMIN = "admin";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addModule(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty("My Awesome Page", null))
                                .withUrl("/pg?page_id=${page.id}")
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.sizeToParentServlet())
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

        ConfluenceViewPage confluenceViewPage = createAndVisitViewPage();

        ConfluenceGeneralPage viewProjectPage = product.getPageBinder().bind(ConfluenceGeneralPage.class, "my-awesome-page", "My Awesome Page", true);

        assertThat(viewProjectPage.isRemotePluginLinkPresent(), is(true));

        URI url = new URI(viewProjectPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/confluence/plugins/servlet/ac/my-plugin/pg"));

        assertThat(URLEncodedUtils.parse(url, UTF_8.name()),
                containsInAnyOrder(
                        (NameValuePair) new BasicNameValuePair("page_id", confluenceViewPage.getPageId())
                )
        );

        RemotePluginTestPage addonContentsPage = viewProjectPage.clickRemotePluginLink();

        assertThat(addonContentsPage.isFullSize(), is(true));
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
        return confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser(ADMIN, ADMIN)), SPACE, "A test page", "some page content");
    }

}
