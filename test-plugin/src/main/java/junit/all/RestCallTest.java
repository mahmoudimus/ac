package junit.all;

import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import services.ServiceAccessor;
import org.junit.Test;

import java.net.URI;

import static services.HttpUtils.sendFailedSignedGet;
import static services.HttpUtils.sendSignedGet;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class RestCallTest
{
    private final SignedRequestHandler signedRequestHandler = ServiceAccessor.getSignedRequestHandler();
    private final String baseUrl = System.getProperty("baseurl");

    @Test
    public void testCall() throws Exception
    {
        String result = sendSignedGet(signedRequestHandler,
                URI.create(baseUrl + "/rest/remoteplugintest/latest/user"), "betty");
        assertEquals("betty", result);
    }

    @Test
    public void testCallAsAnonymousButSigned() throws Exception
    {
        String result = sendSignedGet(signedRequestHandler,
                URI.create(baseUrl + "/rest/remoteplugintest/latest/user"), "");
        assertEquals("", result);
    }

    @Test
    public void testForbiddenCallForUnknownScope() throws Exception
    {
        int status = sendFailedSignedGet(signedRequestHandler,
                URI.create(baseUrl + "/rest/remoteplugintest/latest/unscoped"), "betty");
        assertEquals(403, status);
    }

    @Test
    public void testUnauthorizedCallForUnknownUser() throws Exception
    {
        int status = sendFailedSignedGet(signedRequestHandler,
                URI.create(baseUrl + "/rest/remoteplugintest/latest/unscoped"), "darkstranger");
        assertEquals(401, status);
    }

    @Test
    public void testForbiddenCallForUnrequestedScope() throws Exception
    {
        int status = sendFailedSignedGet(signedRequestHandler,
                URI.create(baseUrl + "/rest/remoteplugintest/latest/unauthorisedscope"), "betty");
        assertEquals(403, status);

    }

}
