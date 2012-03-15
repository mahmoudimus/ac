package junit.all;

import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import org.junit.Test;

import static services.HttpUtils.sendFailedSignedGet;
import static services.HttpUtils.sendSignedGet;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class RestCallTest
{
    private final OAuthContext oAuthContext = new OAuthContext();
    private final String clientKey = Environment.getAllClients().iterator().next();
    @Test
    public void testCall() throws Exception
    {
        String result = sendSignedGet(oAuthContext, oAuthContext.getHostBaseUrl(clientKey) + "/rest/remoteapptest/latest/", "betty");
        assertEquals("betty", result);
    }

    @Test
    public void testUnauthorizedCall() throws Exception
    {
        int status = sendFailedSignedGet(oAuthContext,
                oAuthContext.getHostBaseUrl(clientKey) + "/rest/remoteappforbidden/latest/", "betty");
        assertEquals(403, status);
    }
}
