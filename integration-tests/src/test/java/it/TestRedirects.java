package it;

import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.GeneralPageModule;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static it.servlet.ConnectAppServlets.customMessageServlet;
import static org.junit.Assert.assertEquals;

public class TestRedirects extends AbstractBrowserlessTest
{
    @BeforeClass
    public static void setupUrlHandlers()
    {
        HttpURLConnection.setFollowRedirects(false);
    }

    @Test
    public void testPermanentRedirect() throws Exception
    {
        AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner(baseUrl)
                .add(GeneralPageModule.key("page")
                                      .name("Page")
                                      .path("/page")
                                      .resource(customMessageServlet("message")))
                .start();

        URL url = new URL(baseUrl + "/plugins/servlet/redirect/permanent?app_key=" + runner.getPluginKey() + "&app_url=/page&message=bar");
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, yc.getResponseCode());

        // follow redirect
        String redirectUrl = yc.getHeaderField("Location");
        HttpURLConnection conn = (HttpURLConnection) new URL(redirectUrl).openConnection();
        String responseText = IOUtils.toString(conn.getInputStream());
        assertEquals("bar", responseText);

        runner.stop();
    }

}
