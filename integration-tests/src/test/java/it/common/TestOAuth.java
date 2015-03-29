package it.common;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import com.atlassian.plugin.connect.api.OAuth;
import com.atlassian.plugin.connect.api.service.SignedRequestHandler;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.BaseUrlLocator;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@OAuth
public class TestOAuth
{
    private final String baseUrl = BaseUrlLocator.getBaseUrl();

    @Test
    public void testAuthorizeRequestWorksWithJsonDescriptor() throws Exception
    {
        ConnectRunner runner = null;
        try
        {
            runner = new ConnectRunner(baseUrl, AddonTestUtils.randomAddOnKey())
                    .addOAuth()
                    .addModule("webItems", AddonTestUtils.randomWebItemBean())
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
