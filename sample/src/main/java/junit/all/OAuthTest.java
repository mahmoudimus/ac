package junit.all;

import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import net.oauth.OAuthServiceProvider;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class OAuthTest
{
    private final OAuthContext oAuthContext = new OAuthContext();
    private final String clientKey = Environment.getAllClients().iterator().next();

    @Test
    public void testAuthorizeRequestWorks() throws Exception
    {
        OAuthServiceProvider provider = oAuthContext.getHostConsumer(clientKey)
                .serviceProvider;
        URL url = new URL(provider.requestTokenURL);
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        yc.setDoOutput(true);
        yc.setDoInput(true);
        yc.setRequestMethod("POST");
        oAuthContext.sign(url.toString(), "POST", null, yc);
        yc.getOutputStream().close();
        assertEquals(200, yc.getResponseCode());
    }
}
