package com.atlassian.plugin.connect.test.client;

import cc.plural.jsonij.JSON;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.*;

import static java.util.Collections.singletonList;

public final class AtlassianConnectRestClient
{
    private final String baseUrl;
    private final String defaultUsername;
    private final String defaultPassword;

    public static final String UPM_URL_PATH = "/rest/plugins/1.0/";
    private static final String UPM_TOKEN_HEADER = "upm-token";
    private static final Random RAND = new Random();

    /**
     * Checks the add-on installation status in regular intervals (avoids busy polling)
     */
    private class StatusChecker
    {
        private final String statusUrl;
        private final long timeout;
        private final long period;
        private final ScheduledExecutorService scheduledExecutor;

        private StatusChecker(String statusUrl, long timeout, TimeUnit timeoutUnit, long period, TimeUnit periodUnit)
        {
            this.statusUrl = statusUrl;
            this.timeout = timeoutUnit.toMillis(timeout);
            this.period = periodUnit.toMillis(period);
            this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        }

        public void run() throws Exception
        {
            Callable<Boolean> statusChecker = new Callable<Boolean>()
            {
                @Override
                public Boolean call() throws Exception
                {
                    HttpGet statusGet = new HttpGet(statusUrl);
                    ResponseHandler<String> statusHandler = new BasicResponseHandler();
                    String response = sendRequestAsUser(statusGet, statusHandler, defaultUsername, defaultPassword);
                    if (StringUtils.isNotBlank(response))
                    {
                        JSON json = JSON.parse(response);
                        return (null != json.get("enabled"));
                    }
                    return false;
                }
            };

            ScheduledFuture<Boolean> statusCheck = scheduledExecutor.schedule(statusChecker, period, TimeUnit.MILLISECONDS);

            long abortAfter = System.currentTimeMillis() + timeout;

            while (!statusCheck.get() && abortAfter > System.currentTimeMillis())
            {
                statusCheck = scheduledExecutor.schedule(statusChecker, period, TimeUnit.MILLISECONDS);
            }

            if (abortAfter <= System.currentTimeMillis())
            {
                throw new Exception("Connect App Plugin did not install within the allotted timeout");
            }
        }
    }

    public AtlassianConnectRestClient(String baseUrl, String username, String password)
    {
        this.baseUrl = baseUrl;
        this.defaultUsername = username;
        this.defaultPassword = password;
    }

    public void install(String registerUrl) throws Exception
    {
        //get a upm token
        String token = getUpmToken();

        HttpPost post = new HttpPost(baseUrl + UPM_URL_PATH + "?token=" + token);

        post.addHeader("Accept", "application/json");
        post.setEntity(new StringEntity("{ \"pluginUri\": \"" + registerUrl + "\", \"pluginName\": \"the plugin name\" }", ContentType.create("application/vnd.atl.plugins.install.uri+json")));

        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        String response = sendRequestAsUser(post, responseHandler, defaultUsername, defaultPassword);
        JSON json = JSON.parse(response);

        if (null == json.get("enabled"))
        {
            URI uri = new URI(baseUrl);
            final String statusUrl = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + json.get("links").get("self").getString();

            StatusChecker statusChecker = new StatusChecker(statusUrl, 1, TimeUnit.MINUTES, 500, TimeUnit.MILLISECONDS);
            statusChecker.run();
        }
    }

    public void uninstall(String appKey) throws Exception
    {
        HttpDelete delete = new HttpDelete(getUpmPluginResource(appKey));

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        sendRequestAsUser(delete, responseHandler, defaultUsername, defaultPassword);
    }

    public String getUpmPluginJson(String appKey) throws Exception
    {
        HttpGet get = new HttpGet(getUpmPluginResource(appKey));

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        return sendRequestAsUser(get, responseHandler, defaultUsername, defaultPassword);
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
        HttpResponse response = getDefaultHttpClient(defaultUsername, defaultPassword).execute(upmGet);
        Header[] tokenHeaders = response.getHeaders(UPM_TOKEN_HEADER);

        if (tokenHeaders == null || tokenHeaders.length == 0)
        {
            throw new IOException(getTokenHeaderExceptionMessage("UPM Token Header missing from response", response));
        }

        if (tokenHeaders.length > 1)
        {
            throw new IOException(getTokenHeaderExceptionMessage("Multiple UPM Token Headers found on response", response));
        }

        upmToken = tokenHeaders[0].getValue();
        EntityUtils.consume(response.getEntity());

        return upmToken;
    }

    private String getTokenHeaderExceptionMessage(String prefix, HttpResponse response)
    {
        String responseBody = null;

        try
        {
            responseBody = IOUtils.toString(response.getEntity().getContent());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            responseBody = "<failed to read due to IOException: " + e.getLocalizedMessage() + ">";
        }

        return prefix + ": expected-header-name=" + UPM_TOKEN_HEADER
                + ", headers=" + headersToString(response.getAllHeaders())
                + ", status-code=" + response.getStatusLine().getStatusCode()
                + ", reason=" + response.getStatusLine().getReasonPhrase()
                + ", protocol-version=" + response.getStatusLine().getProtocolVersion()
                + ", response-body=" + responseBody;
    }

    private String headersToString(Header[] tokenHeaders)
    {
        StringBuilder sb = new StringBuilder();

        if (null == tokenHeaders)
        {
            sb.append("null");
        }
        else
        {
            sb.append('[');
            boolean notFirst = false;

            for (Header header : tokenHeaders)
            {
                if (notFirst)
                {
                    sb.append(", ");
                }

                notFirst = true;

                if (null == header)
                {
                    sb.append("null");
                }
                else
                {
                    sb.append(header.getName()).append('=').append(header.getValue());
                }
            }

            sb.append(']');
        }

        return sb.toString();
    }

    public <T> T sendRequestAsUser(HttpRequest request, ResponseHandler<T> handler, String username, String password) throws Exception
    {
        URI uri = new java.net.URI(baseUrl);

        HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local
        // auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

        return getDefaultHttpClient(username, password).execute(targetHost, request, handler, localcontext);
    }

    private DefaultHttpClient getDefaultHttpClient(String username, String password)
    {
        DefaultHttpClient httpclient = new DefaultHttpClient(new SingleClientConnManager());
        httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        return httpclient;
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

    private String getUpmPluginResource(final String appKey)
    {
        return baseUrl + UPM_URL_PATH + appKey + "-key";
    }
}
