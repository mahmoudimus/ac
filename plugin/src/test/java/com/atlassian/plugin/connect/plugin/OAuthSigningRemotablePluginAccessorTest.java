package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.httpclient.api.*;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.license.LicenseStatus;
import com.atlassian.plugin.connect.plugin.util.LocaleHelper;
import com.atlassian.plugin.connect.plugin.util.http.CachingHttpContentRetriever;
import com.atlassian.plugin.connect.plugin.util.http.HttpContentRetriever;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import net.oauth.OAuth;
import org.junit.Test;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class OAuthSigningRemotablePluginAccessorTest
{
    private static final String PLUGIN_KEY = "key";
    private static final String PLUGIN_NAME = "name";
    private static final String BASE_URL = "http://server:1234/contextPath";
    private static final String FULL_PATH_URL = BASE_URL + "/path";
    private static final String OUTGOING_FULL_GET_URL = FULL_PATH_URL + "?param=param+value";
    private static final String GET_FULL_URL = OUTGOING_FULL_GET_URL + "&lic=active&loc=whatever";
    private static final Map<String,String> GET_HEADERS = Collections.singletonMap("header", "header value");
    private static final Map<String,String> GET_PARAMS = Collections.singletonMap("param", "param value");
    private static final Map<String,String[]> GET_PARAMS_STRING_ARRAY = Collections.singletonMap("param", new String[]{"param value"});
    private static final URI FULL_PATH_URI = URI.create(FULL_PATH_URL);
    private static final URI GET_PATH = URI.create("/path");
    private static final URI UNEXPECTED_ABSOLUTE_URI = URI.create("http://www.example.com/path");
    private static final String EXPECTED_GET_RESPONSE = "expected";

    @Test
    public void createdRemotePluginAccessorHasCorrectPluginKey() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().getKey(), is(PLUGIN_KEY));
    }

    @Test
    public void createdRemotePluginAccessorHasCorrectPluginName() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().getName(), is(PLUGIN_NAME));
    }

    @Test
    public void createdRemotePluginAccessorCorrectlyCallsTheHttpContentRetriever() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().executeAsync(HttpMethod.GET, GET_PATH, GET_PARAMS, GET_HEADERS).get(), is(EXPECTED_GET_RESPONSE));
    }

    @Test
    public void createdRemotePluginAccessorCorrectlySignsTheRequestUrl() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().signGetUrl(GET_PATH, Collections.singletonMap("param", new String[]{"value"})),
                is("http://server:1234/contextPath/path?param=value&oauth_nonce=fake_nonce&oauth_version=1.0&oauth_signature_method=RSA-SHA1&oauth_timestamp=fake_timestamp"));
    }

    @Test
    public void createdRemotePluginAccessorHasCorrectBaseUrl() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().getBaseUrl().toString(), is(BASE_URL));
    }

    @Test
    public void createdRemotePluginAccessorCreatesCorrectGetUrl() throws ExecutionException, InterruptedException
    {
        // FIXME: due to a bug in OAuthSigningRemotablePluginAccessor or its dependencies, using multiple parameter values causes only the first value to be used
        assertThat(createRemotePluginAccessor().createGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY), is(OUTGOING_FULL_GET_URL));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createdRemotePluginAccessorThrowsIAEWhenGetUrlIsIncorrectlyAbsolute() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().createGetUrl(UNEXPECTED_ABSOLUTE_URI, GET_PARAMS_STRING_ARRAY), is(OUTGOING_FULL_GET_URL));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createdRemotePluginAccessorThrowsIAEWhenGetUrlIsAbsoluteToAddon() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().createGetUrl(FULL_PATH_URI, GET_PARAMS_STRING_ARRAY), is(OUTGOING_FULL_GET_URL));
    }

    @Test
    public void authorizationGeneratorIsNotNull() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().getAuthorizationGenerator(), is(not(nullValue())));
    }

    private RemotablePluginAccessor createRemotePluginAccessor() throws ExecutionException, InterruptedException
    {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn(PLUGIN_KEY);
        when(plugin.getName()).thenReturn(PLUGIN_NAME);

        Supplier<URI> baseUrlSupplier = new Supplier<URI>()
        {
            @Override
            public URI get()
            {
                return URI.create(BASE_URL);
            }
        };
        OAuthLinkManager oAuthLinkManager = new MockOAuthLinkManager(mock(ServiceProviderConsumerStore.class), mock(AuthenticationConfigurationManager.class), mock(ConsumerService.class, RETURNS_DEEP_STUBS));
        return new OAuthSigningRemotablePluginAccessor(plugin, baseUrlSupplier, createDummyServiceProvider(), mockCachingHttpContentRetriever(), oAuthLinkManager);
    }

    private ServiceProvider createDummyServiceProvider()
    {
        URI dummyUri = URI.create("http://localhost");
        return new ServiceProvider(dummyUri, dummyUri, dummyUri);
    }

    private HttpContentRetriever mockCachingHttpContentRetriever() throws ExecutionException, InterruptedException
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

    private Request.Builder mockRequest(String promisedHttpResponse) throws InterruptedException, ExecutionException
    {
        Request.Builder requestBuilder = mock(Request.Builder.class);
        {
            when(requestBuilder.setHeaders(GET_HEADERS)).thenReturn(requestBuilder);
            when(requestBuilder.setAttributes(any(Map.class))).thenReturn(requestBuilder);
            {
                ResponsePromise responsePromise = mock(ResponsePromise.class);
                when(requestBuilder.execute(any(Request.Method.class))).thenReturn(responsePromise);

                Promise<String> promise = mock(Promise.class);
                when(promise.get()).thenReturn(promisedHttpResponse);
                when(responsePromise.transform(any(ResponseTransformation.class))).thenReturn(promise);
            }
        }
        return requestBuilder;
    }

    private static class MockOAuthLinkManager extends OAuthLinkManager
    {
        public MockOAuthLinkManager(ServiceProviderConsumerStore serviceProviderConsumerStore, AuthenticationConfigurationManager authenticationConfigurationManager, ConsumerService consumerService)
        {
            super(serviceProviderConsumerStore, authenticationConfigurationManager, consumerService);
        }

        @Override
        public List<Map.Entry<String, String>> signAsParameters(ServiceProvider serviceProvider,
                HttpMethod method,
                URI url,
                Map<String, List<String>> originalParams)
        {
            Map<String, List<String>> paramsWithPredicatableOAuthValues = new HashMap<String, List<String>>(originalParams.size());

            for (Map.Entry<String, List<String>> param : originalParams.entrySet())
            {
                if (OAuth.OAUTH_NONCE.equals(param.getKey()))
                {
                    paramsWithPredicatableOAuthValues.put(param.getKey(), Arrays.asList("fake_nonce"));
                }
                else if (OAuth.OAUTH_TIMESTAMP.equals(param.getKey()))
                {
                    paramsWithPredicatableOAuthValues.put(param.getKey(), Arrays.asList("fake_timestamp"));
                }
                else
                {
                    paramsWithPredicatableOAuthValues.put(param.getKey(), param.getValue());
                }
            }

            return newArrayList(Maps.transformValues(paramsWithPredicatableOAuthValues, new Function<List<String>, String>()
            {
                @Override
                public String apply(List<String> strings)
                {
                    // TODO: Doesn't handle multiple values with the same param name
                    return strings.get(0);
                }
            }).entrySet());
        }

        @Override
        public String generateAuthorizationHeader(HttpMethod method,
                ServiceProvider serviceProvider,
                URI url,
                Map<String, List<String>> originalParams)
        {
            return null; // results in no Authorization header being added, allowing us to assert that headers-in == headers-out
        }
    }
}
