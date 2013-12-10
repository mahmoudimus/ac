package it;

import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class TestOAuth extends AbstractBrowserlessTest
{
    @Test
    public void testAuthorizeRequestWorksWithXmlDescriptor() throws Exception
    {
        AtlassianConnectAddOnRunner runner = null;
        try
        {
            runner = new AtlassianConnectAddOnRunner(baseUrl)
                    .addOAuth()
                    .start();

            URL url = new URL(baseUrl + "/plugins/servlet/oauth/request-token");
            HttpURLConnection yc = (HttpURLConnection) url.openConnection();
            yc.setDoOutput(true);
            yc.setDoInput(true);
            yc.setRequestMethod("POST");
            runner.getSignedRequestHandler().get().sign(url.toURI(), "POST", null, yc);
            yc.getOutputStream().close();
            assertEquals(200, yc.getResponseCode());
        }
        finally
        {
            AtlassianConnectAddOnRunner.stopAndUninstallQuietly(runner);
        }
    }

    @Test
    public void testAuthorizeRequestWorksWithJsonDescriptor() throws Exception
    {
        ConnectRunner runner = null;
        try
        {
            runner = new ConnectRunner(baseUrl, "my-plugin")
                    .addOAuth()
                    .start();
            URL url = new URL(baseUrl + "/plugins/servlet/oauth/request-token");
            HttpURLConnection yc = (HttpURLConnection) url.openConnection();
            yc.setDoOutput(true);
            yc.setDoInput(true);
            yc.setRequestMethod("POST");
            runner.getSignedRequestHandler().sign(url.toURI(), "POST", null, yc);
            yc.getOutputStream().close();
            assertEquals(200, yc.getResponseCode());
        }
        finally
        {
            ConnectRunner.stopAndUninstallQuietly(runner);
        }
    }

}
