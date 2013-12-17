package com.atlassian.plugin.connect.plugin;

import com.atlassian.httpclient.api.DefaultResponseTransformation;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponseTransformation;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.license.LicenseStatus;
import com.atlassian.plugin.connect.plugin.util.LocaleHelper;
import com.atlassian.plugin.connect.plugin.util.http.CachingHttpContentRetriever;
import com.atlassian.plugin.connect.plugin.util.http.HttpContentRetriever;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.util.concurrent.Promise;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseSigningRemotablePluginAccessorTest
{
    protected static final String PLUGIN_KEY = "key";
    protected static final String PLUGIN_NAME = "name";
    protected static final String BASE_URL = "http://server:1234/contextPath";
    protected static final String FULL_PATH_URL = BASE_URL + "/path";
    protected static final Map<String, String> GET_HEADERS = Collections.singletonMap("header", "header value");
    protected static final String EXPECTED_GET_RESPONSE = "expected";
    protected static final String OUTGOING_FULL_GET_URL = FULL_PATH_URL + "?param=param+value";
    protected static final String GET_FULL_URL = OUTGOING_FULL_GET_URL + "&lic=active&loc=whatever";

    protected ServiceProvider createDummyServiceProvider()
    {
        URI dummyUri = URI.create("http://localhost");
        return new ServiceProvider(dummyUri, dummyUri, dummyUri);
    }

    protected Plugin mockPlugin()
    {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn(PLUGIN_KEY);
        when(plugin.getName()).thenReturn(PLUGIN_NAME);

        return plugin;
    }

    protected HttpContentRetriever mockCachingHttpContentRetriever()
    {
        LicenseRetriever licenseRetriever = mock(LicenseRetriever.class);
        when(licenseRetriever.getLicenseStatus(PLUGIN_KEY)).thenReturn(LicenseStatus.ACTIVE);

        LocaleHelper localeHelper = mock(LocaleHelper.class);
        when(localeHelper.getLocaleTag()).thenReturn("whatever");

        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        HttpClient httpClient = mockHttpClient(mockRequest(EXPECTED_GET_RESPONSE));
        when(httpClientFactory.create(any(HttpClientOptions.class))).thenReturn(httpClient);

        return new CachingHttpContentRetriever(licenseRetriever, localeHelper, httpClientFactory, mock(PluginRetrievalService.class, RETURNS_DEEP_STUBS));
    }

    private HttpClient mockHttpClient(Request.Builder request)
    {
        HttpClient httpClient = mock(HttpClient.class, RETURNS_DEEP_STUBS);
        when(httpClient.newRequest(GET_FULL_URL)).thenReturn(request);
        when(httpClient.transformation()).thenReturn(DefaultResponseTransformation.builder());
        return httpClient;
    }

    private Request.Builder mockRequest(String promisedHttpResponse)
    {
        Request.Builder requestBuilder = mock(Request.Builder.class);
        {
            when(requestBuilder.setHeaders(GET_HEADERS)).thenReturn(requestBuilder);
            when(requestBuilder.setAttributes(any(Map.class))).thenReturn(requestBuilder);
            {
                ResponsePromise responsePromise = mock(ResponsePromise.class);
                when(requestBuilder.execute(any(Request.Method.class))).thenReturn(responsePromise);

                Promise<String> promise = mockPromise(promisedHttpResponse);
                when(responsePromise.transform(any(ResponseTransformation.class))).thenReturn(promise);
            }
        }
        return requestBuilder;
    }

    private Promise<String> mockPromise(String promisedHttpResponse)
    {
        Promise<String> promise = mock(Promise.class);
        try
        {
            when(promise.get()).thenReturn(promisedHttpResponse);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e);
        }
        return promise;
    }
}
