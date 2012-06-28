package junit.all;

import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import junit.OAuthContextAccessor;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class OAuthTest
{
    private final OAuthContext oAuthContext = OAuthContextAccessor.getOAuthContext();
    private final String baseUrl = System.getProperty("baseurl");

    @Test
    public void testAuthorizeRequestWorks() throws Exception
    {
        URL url = new URL(baseUrl + "/plugins/servlet/oauth/request-token");
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        yc.setDoOutput(true);
        yc.setDoInput(true);
        yc.setRequestMethod("POST");
        oAuthContext.sign(url.toString(), "POST", null, yc);
        yc.getOutputStream().close();
        assertEquals(200, yc.getResponseCode());
    }
}
