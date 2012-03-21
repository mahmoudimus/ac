package junit.all;

import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static services.HttpUtils.sendFailedSignedGet;
import static services.HttpUtils.sendSignedGet;

public class ReadAppLinksScopeTest
{
    private final OAuthContext oAuthContext = new OAuthContext();
    private final String clientKey = Environment.getAllClients().iterator().next();

    @Test
    public void testGetApplicationLink() throws Exception
    {
        String result = sendSignedGet(oAuthContext, oAuthContext.getHostBaseUrl(clientKey) + "/rest/applinks/latest/applicationlink.json", "admin");
        JSONObject j = new JSONObject(result);
        JSONArray applicationLinks = j.getJSONArray("applicationLinks");
        assertEquals(applicationLinks.getJSONObject(0).getString("name"), "Remote App - app1");
    }
}
