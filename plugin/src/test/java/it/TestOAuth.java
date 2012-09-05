package it;

import com.atlassian.labs.remoteapps.spi.Permissions;
import com.atlassian.labs.remoteapps.test.RemoteAppRunner;
import com.atlassian.labs.remoteapps.test.RunnerSignedRequestHandler;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static com.atlassian.labs.remoteapps.test.Utils.createSignedRequestHandler;
import static org.junit.Assert.assertEquals;

public class TestOAuth extends AbstractBrowserlessTest
{
    @Test
    public void testAuthorizeRequestWorks() throws Exception
    {
        RunnerSignedRequestHandler requestHandler = createSignedRequestHandler(
                "authorizeRequestWorks");
        RemoteAppRunner runner = new RemoteAppRunner(baseUrl,
                "authorizeRequestWorks")
                .addOAuth(requestHandler)
                .addPermission(Permissions.CREATE_OAUTH_LINK)
                .start();

        URL url = new URL(baseUrl + "/plugins/servlet/oauth/request-token");
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        yc.setDoOutput(true);
        yc.setDoInput(true);
        yc.setRequestMethod("POST");
        requestHandler.sign(url.toURI(), "POST", null, yc);
        yc.getOutputStream().close();
        assertEquals(200, yc.getResponseCode());

        runner.stop();
    }

}
