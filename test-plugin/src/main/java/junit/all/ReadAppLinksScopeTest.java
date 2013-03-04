package junit.all;

import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import services.ServiceAccessor;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertNotNull;
import static services.HttpUtils.sendSignedGet;
import static util.JsonUtils.parseObject;

public class ReadAppLinksScopeTest
{
    private final SignedRequestHandler signedRequestHandler = ServiceAccessor.getSignedRequestHandler();
    private final String baseUrl = System.getProperty("baseurl");

    @Test
    public void testGetApplicationLink() throws Exception
    {
        String result = sendSignedGet(signedRequestHandler, URI.create(baseUrl + "/rest/applinks/latest/applicationlink.json"), "admin");
        JSONObject j = parseObject(result);
        JSONArray applicationLinks = (JSONArray) j.get("applicationLinks");
        assertNotNull(applicationLinks);
    }
}
