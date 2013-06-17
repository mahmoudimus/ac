package it;

import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.test.RemotePluginRunner;
import com.atlassian.plugin.remotable.test.RunnerSignedRequestHandler;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static com.atlassian.plugin.remotable.test.Utils.createSignedRequestHandler;
import static org.junit.Assert.assertEquals;

public class TestOAuth extends AbstractBrowserlessTest
{
    @Test
    public void testAuthorizeRequestWorks() throws Exception
    {
        RunnerSignedRequestHandler requestHandler = createSignedRequestHandler(
                "authorizeRequestWorks");
        RemotePluginRunner runner = new RemotePluginRunner(baseUrl,
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
