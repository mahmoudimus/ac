package com.atlassian.plugin.connect.test.client;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import cc.plural.jsonij.JSON;

import static java.util.Collections.singletonList;

public class UpmTokenRequestor
{
    public static final String UPM_URL_PATH = "/rest/plugins/1.0/";
    public static final String UPM_TOKEN_HEADER = "upm-token";
    private static final Random RAND = new Random();

    private final long timeout;
    private final long period;
    private final ScheduledExecutorService scheduledExecutor;
    private final UserRequestSender userRequestSender;

    public UpmTokenRequestor(UserRequestSender userRequestSender, long timeout, TimeUnit timeoutUnit, long period, TimeUnit periodUnit)
    {
        this.userRequestSender = userRequestSender;
        this.timeout = timeoutUnit.toMillis(timeout);
        this.period = periodUnit.toMillis(period);
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public String run(final String defaultUsername, final String defaultPassword) throws Exception
    {
        //Confluence has a bug where it will return a 200 with an empty body/headers sometimes in dev mode. Therefore we need to retry
        //until we get a valid response even if we get a 200.
        final StringBuilder upmToken = new StringBuilder();

        Callable<Boolean> tokenChecker = new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                // Perform a GET on the root UPM resource in order to receive a generated XSRF token. We require this token in
                // order to send a valid plugin upload request.
                // UPM does not seem to honour the "X-Atlassian-Token: no-check" header that can normally be used to disable
                // XSRF token checking for a request.
                String authType = URLEncodedUtils.format(singletonList(new BasicNameValuePair("os_authType", "basic")), "UTF-8");
                HttpHead upmTokenRequest = new HttpHead(getUpmPluginsRestURL(true) + "&" + authType);
                upmTokenRequest.addHeader("Accept", "application/vnd.atl.plugins.installed+json"); // UPM returns custom JSON content types.

                HttpResponse response = userRequestSender.getDefaultHttpClient(defaultUsername, defaultPassword).execute(upmTokenRequest);
                Header[] tokenHeaders = response.getHeaders(UPM_TOKEN_HEADER);

                if (tokenHeaders == null || tokenHeaders.length == 0)
                {
                    EntityUtils.consume(response.getEntity());
                    return false;
                }

                if (tokenHeaders.length > 1)
                {
                    throw new IOException(getTokenHeaderExceptionMessage("Multiple UPM Token Headers found on response", response));
                }

                upmToken.append(tokenHeaders[0].getValue());
                EntityUtils.consume(response.getEntity());

                return true;
            }
        };
       

        ScheduledFuture<Boolean> tokenCheck = scheduledExecutor.schedule(tokenChecker, period, TimeUnit.MILLISECONDS);

        long abortAfter = System.currentTimeMillis() + timeout;

        while (!tokenCheck.get() && abortAfter > System.currentTimeMillis())
        {
            tokenCheck = scheduledExecutor.schedule(tokenChecker, period, TimeUnit.MILLISECONDS);
        }

        if (abortAfter <= System.currentTimeMillis())
        {
            throw new Exception("Connect App Plugin did not install within the allotted timeout");
        }

        return upmToken.toString();
    }

    private String getUpmPluginsRestURL(boolean cacheBuster)
    {
        return getURL(userRequestSender.getBaseUrl(), UPM_URL_PATH, cacheBuster);
    }

    private String getURL(String baseURL, String path, boolean cacheBuster)
    {
        boolean removeExtraSlash = baseURL.endsWith("/");
        String url = baseURL.substring(0, baseURL.length() - (removeExtraSlash ? 1 : 0)) + path;
        return url + (cacheBuster ? "?_=" + RAND.nextLong() : "");
    }

    public String getUpmPluginResource(final String appKey)
    {
        return userRequestSender.getBaseUrl() + UPM_URL_PATH + appKey + "-key";
    }

    public static String getUpmPluginResource(final String baseUrl, final String appKey)
    {
        return baseUrl + UPM_URL_PATH + appKey + "-key";
    }

    private String getTokenHeaderExceptionMessage(String prefix, HttpResponse response)
    {
        String responseBody;

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
}
