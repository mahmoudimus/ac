package it;

import com.atlassian.plugin.connect.api.service.SignedRequestHandler;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
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
            assertCanRequestOAuthToken(runner.getSignedRequestHandler().get());
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
            assertCanRequestOAuthToken(runner.getSignedRequestHandler());
        }
        finally
        {
            ConnectRunner.stopAndUninstallQuietly(runner);
        }
    }

    private void assertCanRequestOAuthToken(final SignedRequestHandler signedRequestHandler)
            throws IOException, URISyntaxException
    {
        URL url = new URL(baseUrl + "/plugins/servlet/oauth/request-token");
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        yc.setDoOutput(true);
        yc.setDoInput(true);
        yc.setRequestMethod("POST");
        signedRequestHandler.sign(url.toURI(), "POST", null, yc);
        yc.getOutputStream().close();
        assertEquals(200, yc.getResponseCode());
    }

}
