package it.jira;

import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.remotable.test.server.RunnerSignedRequestHandler;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.OutputSupplier;
import hudson.plugins.jira.soap.JiraSoapService;
import hudson.plugins.jira.soap.JiraSoapServiceServiceLocator;
import org.apache.axis.client.Stub;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.lang.RandomStringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Hashtable;

import static com.atlassian.plugin.remotable.test.Utils.createSignedRequestHandler;
import static com.google.common.io.ByteStreams.copy;
import static com.google.common.io.ByteStreams.newInputStreamSupplier;
import static it.TestConstants.BETTY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

public final class TestJiraScopes
{
    private static String baseUrl;
    private static AtlassianConnectAddOnRunner addOnRunner;
    private static RunnerSignedRequestHandler signedRequestHandler;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        final String appKey = RandomStringUtils.randomAlphanumeric(20);
        baseUrl = OwnerOfTestedProduct.INSTANCE.getProductInstance().getBaseUrl();
        signedRequestHandler = createSignedRequestHandler(appKey);
        addOnRunner = new AtlassianConnectAddOnRunner(baseUrl, appKey)
                .addOAuth(signedRequestHandler)
                .addPermission(Permissions.CREATE_OAUTH_LINK)
                .addPermission("read_users_and_groups")
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (addOnRunner != null)
        {
            addOnRunner.stop();
        }
    }

    @Test
    public void testCallScopedSoapService() throws Exception
    {
        assertEquals(BETTY, getJiraSoapService(BETTY).getUser("", BETTY).getName());
    }

    @Test
    public void testCallScopedJsonRpc() throws Exception
    {
        final HttpURLConnection connection = getHttpURLConnection(getJsonRpcBaseUrl(), BETTY);

        copy(newInputStreamSupplier(getJsonBody(BETTY).getBytes(Charset.forName("UTF-8"))), new OutputSupplier<OutputStream>()
        {
            @Override
            public OutputStream getOutput() throws IOException
            {
                return connection.getOutputStream();
            }
        });

        JSONObject result = (JSONObject) new JSONParser().parse(new InputStreamReader(connection.getInputStream()));
        assertEquals(BETTY, ((JSONObject) result.get("result")).get("name"));
    }

    @Test
    public void testJsonLightCall() throws Exception
    {
        final URI url = URI.create(getJsonRpcBaseUrl() + "/getUser");
        final HttpURLConnection connection = getHttpURLConnection(url, BETTY);

        copy(newInputStreamSupplier(getJsonLightBody(BETTY).getBytes(Charset.forName("UTF-8"))), new OutputSupplier<OutputStream>()
        {
            @Override
            public OutputStream getOutput() throws IOException
            {
                return connection.getOutputStream();
            }
        });

        JSONObject result = (JSONObject) new JSONParser().parse(new InputStreamReader(connection.getInputStream()));
        assertEquals(BETTY, result.get("name"));
    }

    private String getJsonBody(String username)
    {
        return new JSONObject(ImmutableMap.builder()
                .put("jsonrpc", "2.0")
                .put("method", "getUser")
                .put("id", 1)
                .put("params", toArray("", username))
                .build())
                .toJSONString();
    }

    private String getJsonLightBody(String username)
    {
        final JSONArray arr = new JSONArray();
        arr.addAll(asList("", username));
        return arr.toJSONString();
    }

    public static JSONArray toArray(Object... values)
    {
        JSONArray arr = new JSONArray();
        arr.addAll(asList(values));
        return arr;
    }

    private HttpURLConnection getHttpURLConnection(URI url, String username) throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection) new URL(url + "?user_id=" + username).openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        signedRequestHandler.sign(url, "POST", username, conn);
        return conn;
    }

    private JiraSoapService getJiraSoapService(String username) throws ServiceException, MalformedURLException
    {
        final URI url = getSoapServiceBaseUrl();
        JiraSoapService service = new JiraSoapServiceServiceLocator().getJirasoapserviceV2(new URL(url + "?user_id=" + username));
        String authorization = signedRequestHandler.getAuthorizationHeaderValue(url, "POST", username);
        ((Stub) service)._setProperty(HTTPConstants.REQUEST_HEADERS, new Hashtable<String, String>(singletonMap(HTTPConstants.HEADER_AUTHORIZATION, authorization)));
        return service;
    }

    private URI getJsonRpcBaseUrl()
    {
        return getRpcBaseUrl("json-rpc");
    }

    private URI getSoapServiceBaseUrl()
    {
        return getRpcBaseUrl("soap");
    }

    private URI getRpcBaseUrl(String s)
    {
        return URI.create(baseUrl + "/rpc/" + s + "/jirasoapservice-v2");
    }
}
