package it.common.iframe;

import java.net.HttpURLConnection;
import java.net.URL;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.common.MultiProductWebDriverTestBase;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;

public class TestLegacyRedirect extends MultiProductWebDriverTestBase
{
    private static final String ADDON_GENERALPAGE = "ac-general-page";
    private static final String ADDON_GENERALPAGE_NAME = "AC General Page";

    private static ConnectRunner remotePlugin;
    private static final String SOME_QUERY_PARAMS = "foo=bar&bar=foo";
    private static final String SOME_ESCAPED_QUERY_PARAMS = "foo=bar%20blah&bar=foo";

    @BeforeClass
    public static void setupUrlHandlers()
    {
        HttpURLConnection.setFollowRedirects(false);
    }

    @BeforeClass
    public static void startConnectAddon() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .setAuthenticationToNone()
                .addModules("generalPages",
                        newPageBean()
                                .withName(new I18nProperty(ADDON_GENERALPAGE_NAME, null))
                                .withUrl("/pg")
                                .withKey(ADDON_GENERALPAGE)
                                .build()
                )
                .addRoute("/pg", ConnectAppServlets.customMessageServlet("hi"))
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @AfterClass
    public static void tearDownUrlHandlers()
    {
        HttpURLConnection.setFollowRedirects(true);
    }


    @Test
    public void testLegacyPathRedirectWithQueryParams() throws Exception
    {
        URL url = new URL(product.getProductInstance().getBaseUrl() + "/plugins/servlet/atlassian-connect/" +
                remotePlugin.getAddon().getKey() + "/" + ADDON_GENERALPAGE + "?" + SOME_QUERY_PARAMS);

        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, yc.getResponseCode());

        // follow redirect
        String redirectUrlStr = yc.getHeaderField("Location");
        HttpURLConnection conn = (HttpURLConnection) new URL(redirectUrlStr).openConnection();
        assertEquals(HttpStatus.SC_OK, conn.getResponseCode());

        URL redirectUrl = new URL(redirectUrlStr);
        assertEquals(redirectUrl.getQuery(), SOME_QUERY_PARAMS);
    }

    @Test
    public void testLegacyPathRedirectWithoutQueryParams() throws Exception
    {
        URL url = new URL(product.getProductInstance().getBaseUrl() + "/plugins/servlet/atlassian-connect/" +
                remotePlugin.getAddon().getKey() + "/" + ADDON_GENERALPAGE);

        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, yc.getResponseCode());

        // follow redirect
        String redirectUrlStr = yc.getHeaderField("Location");
        HttpURLConnection conn = (HttpURLConnection) new URL(redirectUrlStr).openConnection();
        assertEquals(HttpStatus.SC_OK, conn.getResponseCode());

        URL redirectUrl = new URL(redirectUrlStr);
        assertEquals(redirectUrl.getQuery(), null);
    }

    @Test
    public void testLegacyPathRedirectWithEscapedQueryParams() throws Exception
    {
        URL url = new URL(product.getProductInstance().getBaseUrl() + "/plugins/servlet/atlassian-connect/" +
                remotePlugin.getAddon().getKey() + "/" + ADDON_GENERALPAGE + "?" + SOME_ESCAPED_QUERY_PARAMS);

        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, yc.getResponseCode());

        // follow redirect
        String redirectUrlStr = yc.getHeaderField("Location");
        HttpURLConnection conn = (HttpURLConnection) new URL(redirectUrlStr).openConnection();
        assertEquals(HttpStatus.SC_OK, conn.getResponseCode());

        URL redirectUrl = new URL(redirectUrlStr);
        assertEquals(redirectUrl.getQuery(), SOME_ESCAPED_QUERY_PARAMS);
    }

    /*
     * Note: fragments are not sent to the server so nothing to test there
     */

    @Test
    public void testCanAccessDirectly() throws Exception
    {
        String iframeServletPath = IframeUtils.iframeServletPath(remotePlugin.getAddon().getKey(), ADDON_GENERALPAGE);
        URL url = new URL(product.getProductInstance().getBaseUrl() + iframeServletPath);
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        assertEquals(HttpStatus.SC_OK, yc.getResponseCode());
    }
}
