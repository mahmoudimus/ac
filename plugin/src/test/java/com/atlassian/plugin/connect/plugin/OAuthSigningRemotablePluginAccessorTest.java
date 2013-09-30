package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponseTransformation;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
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
    private static final String GET_FULL_URL = "http://server:1234/contextPath/path?param=param+value&lic=active&loc=whatever";
    private static final Map<String,String> GET_HEADERS = Collections.singletonMap("header", "header value");
    private static final Map<String,String> GET_PARAMS = Collections.singletonMap("param", "param value");
    private static final URI GET_PATH = URI.create("/path");
    public static final String EXPECTED_GET_RESPONSE = "expected";
    public static final String BASE_URL = "http://server:1234/contextPath";

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
        assertThat(createRemotePluginAccessor().createGetUrl(GET_PATH, Collections.singletonMap("param", new String[]{"value1"})), is("http://server:1234/contextPath/path?param=value1"));
    }

    @Test
    public void authorizationGeneratorIsNotNull() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().getAuthorizationGenerator(), is(not(nullValue())));
    }

    private RemotablePluginAccessor createRemotePluginAccessor() throws ExecutionException, InterruptedException
    {
        Supplier<URI> baseUrlSupplier = new Supplier<URI>()
        {
            @Override
            public URI get()
            {
                return URI.create(BASE_URL);
            }
        };
        OAuthLinkManager oAuthLinkManager = new MockOAuthLinkManager(mock(ServiceProviderConsumerStore.class), mock(AuthenticationConfigurationManager.class), mock(ConsumerService.class, RETURNS_DEEP_STUBS));
        return new OAuthSigningRemotablePluginAccessor(PLUGIN_KEY, PLUGIN_NAME, baseUrlSupplier, createDummyServiceProvider(), mockCachingHttpContentRetriever(), oAuthLinkManager);
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

    private HttpClient mockHttpClient(Request request)
    {
        HttpClient httpClient = mock(HttpClient.class, RETURNS_DEEP_STUBS);
        when(httpClient.newRequest(GET_FULL_URL)).thenReturn(request);
        return httpClient;
    }

    private Request mockRequest(String promisedHttpResponse) throws InterruptedException, ExecutionException
    {
        Request request = mock(Request.class);
        {
            when(request.setHeaders(GET_HEADERS)).thenReturn(request);
            when(request.setAttributes(any(Map.class))).thenReturn(request);
            {
                ResponseTransformation responseTransformation = mockResponseTransformation(promisedHttpResponse);
                ResponsePromise responsePromise = mock(ResponsePromise.class);
                when(responsePromise.transform()).thenReturn(responseTransformation);
                when(request.execute(any(Request.Method.class))).thenReturn(responsePromise);
            }
        }
        return request;
    }

    private ResponseTransformation mockResponseTransformation(String promisedHttpResponse) throws InterruptedException, ExecutionException
    {
        ResponseTransformation responseTransformation = mock(ResponseTransformation.class);
        when(responseTransformation.ok(any(Function.class))).thenReturn(responseTransformation);
        when(responseTransformation.forbidden(any(Function.class))).thenReturn(responseTransformation);
        when(responseTransformation.others(any(Function.class))).thenReturn(responseTransformation);
        when(responseTransformation.fail(any(Function.class))).thenReturn(responseTransformation);
        {
            Promise<String> promise = mock(Promise.class);
            when(promise.get()).thenReturn(promisedHttpResponse);
            when(responseTransformation.toPromise()).thenReturn(promise);
        }
        return responseTransformation;
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
