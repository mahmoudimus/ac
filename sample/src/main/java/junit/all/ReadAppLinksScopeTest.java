package junit.all;

import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import services.SignedRequestHandlerAccessor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static services.HttpUtils.sendSignedGet;

public class ReadAppLinksScopeTest
{
    private final SignedRequestHandler signedRequestHandler = SignedRequestHandlerAccessor.getSignedRequestHandler();
    private final String baseUrl = System.getProperty("baseurl");

    @Test
    public void testGetApplicationLink() throws Exception
    {
        String result = sendSignedGet(signedRequestHandler, baseUrl + "/rest/applinks/latest/applicationlink.json", "admin");
        JSONObject j = new JSONObject(result);
        JSONArray applicationLinks = j.getJSONArray("applicationLinks");
        assertNotNull(applicationLinks);
    }
}
