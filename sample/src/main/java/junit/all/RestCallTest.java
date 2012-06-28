package junit.all;

import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import junit.OAuthContextAccessor;
import org.junit.Test;

import static services.HttpUtils.sendFailedSignedGet;
import static services.HttpUtils.sendSignedGet;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class RestCallTest
{
    private final OAuthContext oAuthContext = OAuthContextAccessor.getOAuthContext();
    private final String baseUrl = System.getProperty("baseurl");

    @Test
    public void testCall() throws Exception
    {
        String result = sendSignedGet(oAuthContext, baseUrl + "/rest/remoteapptest/latest/user", "betty");
        assertEquals("betty", result);
    }

    @Test
    public void testForbiddenCallForUnknownScope() throws Exception
    {
        int status = sendFailedSignedGet(oAuthContext,
                baseUrl + "/rest/remoteapptest/latest/unscoped", "betty");
        assertEquals(403, status);
    }

    @Test
    public void testUnauthorizedCallForUnknownUser() throws Exception
    {
        int status = sendFailedSignedGet(oAuthContext,
                baseUrl + "/rest/remoteapptest/latest/unscoped", "darkstranger");
        assertEquals(401, status);
    }

    @Test
    public void testForbiddenCallForUnrequestedScope() throws Exception
    {
        int status = sendFailedSignedGet(oAuthContext,
                baseUrl + "/rest/remoteapptest/latest/unauthorisedscope", "betty");
        assertEquals(403, status);

    }

}
