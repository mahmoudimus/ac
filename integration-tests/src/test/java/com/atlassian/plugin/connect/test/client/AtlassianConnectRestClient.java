package com.atlassian.plugin.connect.test.client;

import java.io.IOException;
import java.net.URI;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import cc.plural.jsonij.JSON;

import static java.util.Collections.singletonList;

public final class AtlassianConnectRestClient
{
    private final String baseUrl;
    private final DefaultHttpClient httpclient;
    public static final String UPM_URL_PATH = "/rest/plugins/1.0/";
    private static final String UPM_TOKEN_HEADER = "upm-token";
    private static final Random RAND = new Random();

    public AtlassianConnectRestClient(String baseUrl, String username, String password)
    {
        this.baseUrl = baseUrl;
        httpclient = new DefaultHttpClient(new SingleClientConnManager());
        httpclient.getCredentialsProvider().setCredentials(
                AuthScope.ANY, new UsernamePasswordCredentials(username, password));
    }

    public void install(String registerUrl) throws Exception
    {
        //get a upm token
        String token = getUpmToken();

        HttpPost post = new HttpPost(baseUrl + UPM_URL_PATH + "?token=" + token);

        post.addHeader("Accept", "application/json");
        post.setEntity(new StringEntity("{ \"pluginUri\": \"" + registerUrl + "\", \"pluginName\": \"the plugin name\" }", ContentType.create("application/vnd.atl.plugins.install.uri+json")));

        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        String response = httpclient.execute(post, responseHandler);

        JSON json = JSON.parse(response);
        boolean done = (null != json.get("enabled"));
        int timeout = 5000;

        while (!done && timeout > 1)
        {
            URI uri = new URI(baseUrl);
            String statusUrl = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + json.get("links").get("self").getString();
            HttpGet statusGet = new HttpGet(statusUrl);
            ResponseHandler<String> statusHandler = new BasicResponseHandler();
            response = httpclient.execute(statusGet, statusHandler);

            json = JSON.parse(response);
            done = (null != json.get("enabled"));
            timeout--;
        }

        if (timeout < 2)
        {
            throw new Exception("Connect App Plugin did not install within the allotted timeout!!!");
        }
    }

    public void uninstall(String appKey) throws IOException
    {
        String token = getUpmToken();
        HttpDelete post = new HttpDelete(baseUrl + UPM_URL_PATH + appKey + "-key");

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String response = httpclient.execute(post, responseHandler);
    }

    private String getUpmToken() throws IOException
    {
        // Perform a GET on the root UPM resource in order to receive a generated XSRF token. We require this token in
        // order to send a valid plugin upload request.
        // UPM does not seem to honour the "X-Atlassian-Token: no-check" header that can normally be used to disable
        // XSRF token checking for a request.
        HttpGet upmGet = new HttpGet(getUpmPluginsRestURL(baseUrl, true) + "&" +
                URLEncodedUtils.format(singletonList(new BasicNameValuePair("os_authType", "basic")),
                        "UTF-8"));
        upmGet.addHeader("Accept", "application/vnd.atl.plugins.installed+json"); // UPM returns custom JSON content types.
        String upmToken;
        HttpResponse response = httpclient.execute(upmGet);
        Header[] tokenHeaders = response.getHeaders(UPM_TOKEN_HEADER);
        if (tokenHeaders == null || tokenHeaders.length != 1)
        {
            throw new IOException("UPM Token Header missing from response.");
        }
        upmToken = tokenHeaders[0].getValue();
        EntityUtils.consume(response.getEntity());

        return upmToken;
    }

    private static String getUpmPluginsRestURL(String baseURL, boolean cacheBuster)
    {
        return getURL(baseURL, UPM_URL_PATH, cacheBuster);
    }

    private static String getURL(String baseURL, String path, boolean cacheBuster)
    {
        boolean removeExtraSlash = baseURL.endsWith("/");
        String url = baseURL.substring(0, baseURL.length() - (removeExtraSlash ? 1 : 0)) + path;
        return url + (cacheBuster ? "?_=" + RAND.nextLong() : "");
    }
}
