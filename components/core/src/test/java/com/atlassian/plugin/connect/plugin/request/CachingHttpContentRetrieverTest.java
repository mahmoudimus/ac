package com.atlassian.plugin.connect.plugin.request;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponseTransformation;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.connect.api.auth.AuthorizationGenerator;
import com.atlassian.plugin.connect.api.request.HttpContentRetriever;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.features.DarkFeatureManager;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CachingHttpContentRetrieverTest {
    private HttpContentRetriever httpContentRetriever;
    private InputStream entityStream;
    private AtomicReference<String> actualEntity;

    @Mock
    private AuthorizationGenerator authorizationGenerator;
    @Mock
    private ConnectHttpClientFactory httpClientFactory;
    @Mock
    private HttpClient httpClient;
    @Mock
    private Request.Builder requestBuilder;
    @Mock
    private ResponseTransformation<String> responseTransformation;
    @Mock
    private ResponseTransformation.Builder<String> responseTransformationBuilder;
    @Mock
    private ResponsePromise responsePromise;
    @Mock
    private PluginRetrievalService pluginRetrievalService;
    @Mock
    private Plugin plugin;
    @Mock
    private PluginInformation pluginInformation;
    @Mock
    private DarkFeatureManager darkFeatureManager;

    private static final ImmutableMap<String, String[]> PARAMS = ImmutableMap.of("param", new String[]{"value"});
    private static final Map<String, String> HEADERS = ImmutableMap.of("some", "header");
    private static final URI URL = URI.create("https://example.com/path");


    @Test
    public void baseUrlIsPassedToAuthGenerator() {
        httpContentRetriever.async(authorizationGenerator, HttpMethod.GET, URL, PARAMS, HEADERS, entityStream, "add-on key");
        verify(authorizationGenerator).generate(HttpMethod.GET, URL, PARAMS);
    }

    @Test
    public void testFormUrlEncodedContentTypeHandledCorrectly() {
        httpContentRetriever.async(authorizationGenerator, HttpMethod.POST, URL, PARAMS, HEADERS, entityStream, "add-on key");
        verify(requestBuilder).setContentType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
        assertThat(actualEntity.get(), containsString("body entity"));
        assertThat(actualEntity.get(), containsString("param=value"));
    }

    @Test
    public void testApplicationJsonContentTypeHandledCorrectly() {
        ImmutableMap.Builder<String, String> headers = ImmutableMap.<String, String>builder()
                .putAll(HEADERS)
                .put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

        httpContentRetriever.async(authorizationGenerator, HttpMethod.POST, URL, PARAMS, headers.build(), entityStream, "add-on key");
        verify(requestBuilder).setContentType(ContentType.APPLICATION_JSON.getMimeType());
        assertThat(actualEntity.get(), containsString("body entity"));
        assertThat(actualEntity.get(), not(containsString("param=value")));
    }

    @Before
    public void beforeEachTest() {
        entityStream = IOUtils.toInputStream("body entity");
        actualEntity = new AtomicReference<>();
        when(authorizationGenerator.generate(any(HttpMethod.class), any(URI.class), anyMap())).thenReturn(Optional.empty());
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
        when(requestBuilder.setEntityStream(any(InputStream.class))).thenAnswer(a -> {
            actualEntity.set(IOUtils.toString(((InputStream) a.getArguments()[0])));
            return requestBuilder;
        });
        httpContentRetriever = new CachingHttpContentRetriever(httpClientFactory);
    }
}
