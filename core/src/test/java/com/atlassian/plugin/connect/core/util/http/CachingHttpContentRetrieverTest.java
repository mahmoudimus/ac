package com.atlassian.plugin.connect.core.util.http;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponseTransformation;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.core.ConnectHttpClientFactory;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.util.http.HttpContentRetriever;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.features.DarkFeatureManager;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CachingHttpContentRetrieverTest
{
    private HttpContentRetriever httpContentRetriever;

    @Mock private AuthorizationGenerator authorizationGenerator;
    @Mock private ConnectHttpClientFactory httpClientFactory;
    @Mock private HttpClient httpClient;
    @Mock private Request.Builder requestBuilder;
    @Mock private ResponseTransformation<String> responseTransformation;
    @Mock private ResponseTransformation.Builder<String> responseTransformationBuilder;
    @Mock private ResponsePromise responsePromise;
    @Mock private PluginRetrievalService pluginRetrievalService;
    @Mock private Plugin plugin;
    @Mock private PluginInformation pluginInformation;
    @Mock private DarkFeatureManager darkFeatureManager;

    private static final ImmutableMap<String, String[]> PARAMS = ImmutableMap.of("param", new String[]{"value"});
    private static final Map<String, String> HEADERS = ImmutableMap.of("some", "header");

    @Test
    public void baseUrlIsPassedToAuthGenerator()
    {
        final URI url = URI.create("https://example.com/path");
        final URI baseUrl = URI.create("https://example.com");
        httpContentRetriever.async(authorizationGenerator, HttpMethod.GET, url, PARAMS, HEADERS, "add-on key");
        verify(authorizationGenerator).generate(HttpMethod.GET, url, PARAMS);
    }

    @Before
    public void beforeEachTest()
    {
        when(authorizationGenerator.generate(any(HttpMethod.class), any(URI.class), anyMap())).thenReturn(Option.<String>none());
        when(pluginRetrievalService.getPlugin()).thenReturn(plugin);
        when(plugin.getPluginInformation()).thenReturn(pluginInformation);
        when(httpClientFactory.getInstance()).thenReturn(httpClient);
        when(httpClient.newRequest(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.setAttributes(anyMap())).thenReturn(requestBuilder);
        when(requestBuilder.setHeaders(anyMap())).thenReturn(requestBuilder);
        when(requestBuilder.execute(any(Request.Method.class))).thenReturn(responsePromise);
        when(httpClient.<String>transformation()).thenReturn(responseTransformationBuilder);
        when(responseTransformationBuilder.ok(any(Function.class))).thenReturn(responseTransformationBuilder);
        when(responseTransformationBuilder.forbidden(any(Function.class))).thenReturn(responseTransformationBuilder);
        when(responseTransformationBuilder.others(any(Function.class))).thenReturn(responseTransformationBuilder);
        when(responseTransformationBuilder.fail(any(Function.class))).thenReturn(responseTransformationBuilder);
        when(responseTransformationBuilder.build()).thenReturn(responseTransformation);
        httpContentRetriever = new CachingHttpContentRetriever(httpClientFactory);
    }
}
