package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import net.oauth.OAuth;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class OAuthSigningRemotablePluginAccessorTest extends BaseSigningRemotablePluginAccessorTest
{
    private static final Map<String, String[]> GET_PARAMS_STRING_ARRAY = Collections.singletonMap("param", new String[]{"param value"});
    private static final URI FULL_PATH_URI = URI.create(FULL_PATH_URL);
    private static final URI GET_PATH = URI.create("/path");
    private static final URI UNEXPECTED_ABSOLUTE_URI = URI.create("http://www.example.com/path");

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
        assertThat(createRemotePluginAccessor().executeAsync(HttpMethod.GET, GET_PATH, GET_PARAMS_STRING_ARRAY, UNAUTHED_GET_HEADERS).get(), is(EXPECTED_GET_RESPONSE));
    }

    @Test
    public void createdRemotePluginAccessorCorrectlySignsTheRequestUrl() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().signGetUrl(GET_PATH, Collections.singletonMap("param", new String[]{"value"})),
                is("http://server:1234/contextPath/path?param=value&oauth_signature_method=RSA-SHA1&oauth_nonce=fake_nonce&oauth_version=1.0&oauth_timestamp=fake_timestamp"));
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

    @Test
    public void slashesAreNormalizedOnConcatenation() throws Exception
    {
        RemotablePluginAccessor accessor = createRemotePluginAccessor("https://example.com/addon/");
        assertThat(accessor.createGetUrl(URI.create("/handler"), Collections.<String,String[]>emptyMap()), is("https://example.com/addon/handler"));
    }

    @Test
    public void trailingSlashesAreLeftIntact() throws Exception
    {
        RemotablePluginAccessor accessor = createRemotePluginAccessor("https://example.com/addon");
        assertThat(accessor.createGetUrl(URI.create("/handler/"), Collections.<String,String[]>emptyMap()), is("https://example.com/addon/handler/"));
    }

    private RemotablePluginAccessor createRemotePluginAccessor() throws ExecutionException, InterruptedException
    {
        return createRemotePluginAccessor(BASE_URL);
    }

    private RemotablePluginAccessor createRemotePluginAccessor(final String baseUrl) throws ExecutionException, InterruptedException
    {
        Supplier<URI> baseUrlSupplier = new Supplier<URI>()
        {
            @Override
            public URI get()
            {
                return URI.create(baseUrl);
            }
        };
        OAuthLinkManager oAuthLinkManager = new MockOAuthLinkManager(mock(ServiceProviderConsumerStore.class), mock(AuthenticationConfigurationManager.class), mock(ConsumerService.class, RETURNS_DEEP_STUBS));
        return new OAuthSigningRemotablePluginAccessor(mockPlugin(), baseUrlSupplier, createDummyServiceProvider(),
                mockCachingHttpContentRetriever(), oAuthLinkManager);
    }

    @Override
    protected Map<String, String> getPostSigningHeaders(Map<String, String> preSigningHeaders)
    {
        return preSigningHeaders;
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
            Map<String, List<String>> paramsWithPredicatableOAuthValues = new LinkedHashMap<String, List<String>>(originalParams.size());

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
