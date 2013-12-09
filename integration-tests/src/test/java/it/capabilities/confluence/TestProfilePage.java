package it.capabilities.confluence;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceUserProfilePage;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import it.TestConstants;
import it.confluence.ConfluenceWebDriverTestBase;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean.newPageBean;
import static com.google.common.base.Charsets.UTF_8;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test of profile page in Confluence
 */
public class TestProfilePage extends ConfluenceWebDriverTestBase
{
    private static final String PLUGIN_KEY = "my-plugin";

    private static ConnectCapabilitiesRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addCapability(
                        "profilePages",
                        newPageBean()
                                .withName(new I18nProperty("My Awesome Page", null))
                                .withUrl("/pg")
                                .withWeight(1234)
//                                .withLocation("system.user.profile.links")
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

//    @Test
//    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
//    {
//        loginAsAdmin();
//
//        product.visit(ConfluenceUserProfilePage.class, TestConstants.BARNEY_USERNAME);
//
//        ConfluenceUserProfilePage viewProfilePage = product.getPageBinder().bind(ConfluenceUserProfilePage.class, "my-awesome-page");
//
////        assertThat(viewProfilePage.isRemotePluginLinkPresent(), is(true));
//
//        URI url = new URI(viewProfilePage.getRemotePluginLinkHref());
//        assertThat(url.getPath(), is("/confluence/plugins/servlet/ac/my-plugin/pg"));
//
//        assertThat(URLEncodedUtils.parse(url, UTF_8.name()),
//                containsInAnyOrder(
//                        (NameValuePair) new BasicNameValuePair("project_key", project.getKey()),
//                        new BasicNameValuePair("project_id", project.getId())
//                )
//        );
//
//        RemotePluginTestPage addonContentsPage = viewProfilePage.clickRemotePluginLink();
//        assertThat(addonContentsPage.isFullSize(), is(true));
//    }


}
