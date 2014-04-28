package it;

import java.net.HttpURLConnection;
import java.net.URL;

import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.GeneralPageModule;
import it.servlet.ConnectAppServlets;
import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;

// Tests the inverse of the Json test of the same name. i.e. that the redirect is not applied for xml descriptors
public class TestLegacyRedirect extends AbstractBrowserlessTest
{
    private static final String ADDON_GENERALPAGE = "ac-general-page";
    private static final String ADDON_GENERALPAGE_NAME = "AC General Page";

    private static AtlassianConnectAddOnRunner remotePlugin;

    @BeforeClass
    public static void setupUrlHandlers()
    {
        HttpURLConnection.setFollowRedirects(false);
    }

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .add(GeneralPageModule.key(ADDON_GENERALPAGE)
                        .name(ADDON_GENERALPAGE_NAME)
                        .path("/pg")
                        .resource(ConnectAppServlets.customMessageServlet("hi")))
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
    public void testLegacyPathDoesNotRedirect() throws Exception
    {
        URL url = new URL(product.getProductInstance().getBaseUrl() + "/plugins/servlet/atlassian-connect/" +
                remotePlugin.getPluginKey() + "/" + ADDON_GENERALPAGE);
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        assertEquals(HttpStatus.SC_OK, yc.getResponseCode());
    }

    @Test
    public void testCannotAccessDirectly() throws Exception
    {
        URL url = new URL(product.getProductInstance().getBaseUrl() + "/plugins/servlet/ac/" +
                remotePlugin.getPluginKey() + "/" + ADDON_GENERALPAGE);
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        assertEquals(HttpStatus.SC_NOT_FOUND, yc.getResponseCode());
    }

}
