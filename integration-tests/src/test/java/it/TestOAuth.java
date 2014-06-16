package it;

import com.atlassian.plugin.connect.api.service.SignedRequestHandler;
import com.atlassian.plugin.connect.api.xmldescriptor.OAuth;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import static com.atlassian.plugin.connect.test.RemotePluginUtils.randomWebItemBean;
import static org.junit.Assert.assertEquals;

@OAuth
public class TestOAuth extends AbstractBrowserlessTest
{
    @Test
    public void testAuthorizeRequestWorksWithJsonDescriptor() throws Exception
    {
        ConnectRunner runner = null;
        try
        {
            runner = new ConnectRunner(baseUrl, "my-plugin")
                    .addOAuth()
                    .addModule("webItems",randomWebItemBean())
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
