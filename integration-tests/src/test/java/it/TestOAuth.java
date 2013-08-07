package it;

import java.net.HttpURLConnection;
import java.net.URL;

import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestOAuth extends AbstractBrowserlessTest
{
    @Test
    public void testAuthorizeRequestWorks() throws Exception
    {
        AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner(baseUrl)
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

        runner.stop();
    }

}
