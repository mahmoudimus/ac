package junit.jira;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceServiceLocator;
import com.atlassian.jira.rpc.soap.client.RemoteUser;
import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import org.apache.axis.client.Stub;
import org.apache.axis.transport.http.HTTPConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class JiraReadUsersAndGroupsScopeTest
{
    private OAuthContext oAuthContext = new OAuthContext();
    private final String hostBaseUrl = oAuthContext.getHostBaseUrl(Environment.getAllClients().iterator().next());

    @Test
    public void testCall() throws Exception
    {
        JiraSoapServiceServiceLocator locator = new JiraSoapServiceServiceLocator();
        String url = hostBaseUrl + "/rpc/soap/jirasoapservice-v2";
        JiraSoapService service = locator.getJirasoapserviceV2(new URL(url + "?user_id=betty"));

        String authorization = oAuthContext.getAuthorizationHeaderValue(url, "POST", "betty");
        ((Stub)service)._setProperty(HTTPConstants.REQUEST_HEADERS, new Hashtable(singletonMap(HTTPConstants.HEADER_AUTHORIZATION, authorization)));

        RemoteUser user = service.getUser("", "betty");
        assertEquals("betty", user.getName());
    }

    @Test
    public void testJsonCall() throws Exception
    {
        String url = hostBaseUrl + "/rpc/json-rpc/jirasoapservice-v2";
        HttpURLConnection conn = (HttpURLConnection) new URL(url + "?user_id=betty").openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        oAuthContext.sign(url, "POST", "betty", conn);
        PrintWriter out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()));
        String body = new JSONObject()
                .put("jsonrpc", "2.0")
                .put("method", "getUser")
                .put("id", 1)
                .put("params",
                        new JSONArray()
                                .put("")
                                .put("betty"))
                .toString(2);
        System.out.println("sending body: " + body);
        out.write(body);
        out.close();

        JSONObject result = new JSONObject(new JSONTokener(new InputStreamReader(conn.getInputStream())));
        System.out.println("response: " + result.toString(2));
        assertEquals("betty", result.getJSONObject("result").getString("name"));
    }

    @Test
    public void testJsonLightCall() throws Exception
    {
        String url = hostBaseUrl + "/rpc/json-rpc/jirasoapservice-v2/getUser";
        HttpURLConnection conn = (HttpURLConnection) new URL(url + "?user_id=betty").openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        oAuthContext.sign(url, "POST", "betty", conn);
        PrintWriter out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()));
        String body = new JSONArray()
                                .put("")
                                .put("betty")
                .toString(2);
        System.out.println("sending body: " + body);
        out.write(body);
        out.close();

        JSONObject result = new JSONObject(new JSONTokener(new InputStreamReader(conn.getInputStream())));
        System.out.println("response: " + result.toString(2));
        assertEquals("betty", result.getString("name"));
    }
}
