package com.atlassian.plugin.connect.plugin.request;

import com.atlassian.httpclient.api.DefaultResponseTransformation;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponseTransformation;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.request.HttpContentRetriever;
import com.atlassian.util.concurrent.Promise;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseSigningRemotablePluginAccessorTest {
    protected static final String PLUGIN_KEY = "key";
    protected static final String PLUGIN_NAME = "name";
    protected static final String CONTEXT_PATH = "/contextPath";
    protected static final String BASE_URL = "http://server:1234" + CONTEXT_PATH;
    protected static final String FULL_PATH_URL = BASE_URL + "/path";
    protected static final Map<String, String> UNAUTHED_GET_HEADERS = Collections.singletonMap("header", "header value");
    protected static final String EXPECTED_GET_RESPONSE = "expected";
    protected static final String OUTGOING_FULL_GET_URL = FULL_PATH_URL + "?param=param+value";
    protected static final String GET_FULL_URL = OUTGOING_FULL_GET_URL;

    @Mock
    private Promise<String> promiseMock;

    protected abstract Map<String, String> getPostSigningHeaders(Map<String, String> preSigningHeaders);

    protected ServiceProvider createDummyServiceProvider() {
        URI dummyUri = URI.create("http://localhost");
        return new ServiceProvider(dummyUri, dummyUri, dummyUri);
    }

    protected Plugin mockPlugin() {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn(PLUGIN_KEY);
        when(plugin.getName()).thenReturn(PLUGIN_NAME);

        return plugin;
    }

    protected HttpContentRetriever mockCachingHttpContentRetriever() {
        ConnectHttpClientFactory httpClientFactory = mock(ConnectHttpClientFactory.class);
        HttpClient httpClient = mockHttpClient(mockRequest(EXPECTED_GET_RESPONSE));
        when(httpClientFactory.getInstance()).thenReturn(httpClient);

        return new CachingHttpContentRetriever(httpClientFactory);
    }

    private HttpClient mockHttpClient(Request.Builder request) {
        HttpClient httpClient = mock(HttpClient.class, RETURNS_DEEP_STUBS);
        when(httpClient.newRequest(GET_FULL_URL)).thenReturn(request);
        when(httpClient.transformation()).thenReturn(DefaultResponseTransformation.builder());
        return httpClient;
    }

    private Request.Builder mockRequest(String promisedHttpResponse) {
        Request.Builder requestBuilder = mock(Request.Builder.class);
        {
            when(requestBuilder.setHeaders(getPostSigningHeaders(UNAUTHED_GET_HEADERS))).thenReturn(requestBuilder);
            when(requestBuilder.setAttributes(Mockito.<Map<String, String>>any())).thenReturn(requestBuilder);
            {
                ResponsePromise responsePromise = mock(ResponsePromise.class);
                when(requestBuilder.execute(any(Request.Method.class))).thenReturn(responsePromise);

                Promise<String> promise = mockPromise(promisedHttpResponse);
                when(responsePromise.transform(Mockito.<ResponseTransformation<String>>any())).thenReturn(promise);
            }
        }
        return requestBuilder;
    }

    private Promise<String> mockPromise(String promisedHttpResponse) {
        try {
            when(promiseMock.get()).thenReturn(promisedHttpResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return promiseMock;
    }
}
