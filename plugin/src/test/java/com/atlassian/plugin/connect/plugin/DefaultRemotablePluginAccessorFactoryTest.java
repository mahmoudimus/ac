package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.event.ApplicationLinkAddedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkDeletedEvent;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponseTransformation;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.license.LicenseStatus;
import com.atlassian.plugin.connect.plugin.module.applinks.RemotePluginContainerModuleDescriptor;
import com.atlassian.plugin.connect.plugin.util.LocaleHelper;
import com.atlassian.plugin.connect.plugin.util.http.CachingHttpContentRetriever;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.applinks.RemotePluginContainerApplicationType;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import net.oauth.OAuth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRemotablePluginAccessorFactoryTest
{
    private static final String PLUGIN_KEY = "plugin key";
    private static final String PLUGIN_NAME = "plugin name";
    private static final String GET_FULL_URL = "http://server:1234/contextPath/path?param=param+value&lic=active&loc=whatever";
    private static final Map<String,String> GET_HEADERS = Collections.singletonMap("header", "header value");
    private static final Map<String,String> GET_PARAMS = Collections.singletonMap("param", "param value");
    private static final URI GET_PATH = URI.create("/path");
    public static final String EXPECTED_GET_RESPONSE = "expected";
    public static final String DISPLAY_URL = "http://server:1234/contextPath";

    @Mock private ApplicationLinkAccessor applicationLinkAccessor;
    @Mock private PluginAccessor pluginAccessor;
    @Mock private ApplicationProperties applicationProperties;
    @Mock private EventPublisher eventPublisher;
    @Mock private ConnectAddOnIdentifierService connectIdentifier;

    @Mock LicenseRetriever licenseRetriever;
    @Mock LocaleHelper localeHelper;
    @Mock HttpClientFactory httpClientFactory;
    @Mock PluginRetrievalService pluginRetrievalService;

    private DefaultRemotablePluginAccessorFactory factory;

    @Before
    public void beforeEachTest() throws ExecutionException, InterruptedException
    {
        Plugin plugin = mockPlugin();
        when(pluginAccessor.getPlugin(PLUGIN_KEY)).thenReturn(plugin);

        HttpClient httpClient = mockHttpClient(mockRequest(EXPECTED_GET_RESPONSE));
        when(httpClientFactory.create(any(HttpClientOptions.class))).thenReturn(httpClient);

        when(licenseRetriever.getLicenseStatus(PLUGIN_KEY)).thenReturn(LicenseStatus.ACTIVE);
        when(localeHelper.getLocaleTag()).thenReturn("whatever");
        factory = new DefaultRemotablePluginAccessorFactory(applicationLinkAccessor, mockOAuthLinkManager(), mockCachingHttpContentRetriever(), pluginAccessor, applicationProperties, eventPublisher, connectIdentifier);
    }

    private OAuthLinkManager mockOAuthLinkManager()
    {
        return new MockOAuthLinkManager(mock(ServiceProviderConsumerStore.class), mock(AuthenticationConfigurationManager.class), mock(ConsumerService.class, RETURNS_DEEP_STUBS));
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

    @Test
    public void createdRemotePluginAccessorIsNotNull()
    {
        assertThat(createRemotePluginAccessor(), is(notNullValue()));
    }

    @Test
    public void createdRemotePluginAccessorHasCorrectPluginKey()
    {
        assertThat(createRemotePluginAccessor().getKey(), is(PLUGIN_KEY));
    }

    @Test
    public void createdRemotePluginAccessorHasCorrectPluginName()
    {
        assertThat(createRemotePluginAccessor().getName(), is(PLUGIN_NAME));
    }

    @Test
    public void createdRemotePluginAccessorCorrectlyCallsTheHttpContentRetriever() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().executeAsync(HttpMethod.GET, GET_PATH, GET_PARAMS, GET_HEADERS).get(), is(EXPECTED_GET_RESPONSE));
    }

    @Test
    public void createdRemotePluginAccessorCorrectlySignsTheRequestUrl()
    {
        assertThat(createRemotePluginAccessor().signGetUrl(GET_PATH, Collections.singletonMap("param", new String[]{"value"})),
                is("http://server:1234/contextPath/path?param=value&oauth_nonce=fake_nonce&oauth_version=1.0&oauth_signature_method=RSA-SHA1&oauth_timestamp=fake_timestamp"));
    }

    @Test
    public void createdRemotePluginAccessorHasCorrectDisplayUrl()
    {
        assertThat(createRemotePluginAccessor().getDisplayUrl().toString(), is(DISPLAY_URL));
    }

    @Test
    public void createdRemotePluginAccessorCreatesCorrectGetUrl()
    {
        // FIXME: using multiple parameter values causes only the first value to be used
        assertThat(createRemotePluginAccessor().createGetUrl(GET_PATH, Collections.singletonMap("param", new String[]{"value1"})), is("http://server:1234/contextPath/path?param=value1"));
    }

    @Test
    public void eventListenerIsUnregisteredOnDestruction() throws Exception
    {
        factory.destroy();
        verify(eventPublisher).unregister(factory);
    }

    @Test
    public void getterRepeatedlyReturnsTheSameAccessor()
    {
        assertThat(factory.get(PLUGIN_KEY), is(sameInstance(factory.get(PLUGIN_KEY))));
    }

    @Test
    public void getterReturnsTheSameAccessorAfterAnIrreleventAppLinkTypeIsCreated()
    {
        RemotablePluginAccessor firstAccessor = factory.get(PLUGIN_KEY);
        ApplicationLinkAddedEvent event = mock(ApplicationLinkAddedEvent.class);
        when(event.getApplicationType()).thenReturn(mock(ApplicationType.class));
        factory.onApplicationLinkCreated(event);
        assertThat(factory.get(PLUGIN_KEY), is(sameInstance(firstAccessor)));
    }

    @Test
    public void getterReturnsADifferentAccessorAfterARemotePluginAppLinkIsCreated()
    {
        RemotablePluginAccessor firstAccessor = factory.get(PLUGIN_KEY);
        ApplicationLinkAddedEvent event = mock(ApplicationLinkAddedEvent.class);
        when(event.getApplicationType()).thenReturn(mock(RemotePluginContainerApplicationType.class));
        factory.onApplicationLinkCreated(event);
        assertThat(factory.get(PLUGIN_KEY), is(not(sameInstance(firstAccessor))));
    }

    @Test
    public void getterReturnsTheSameAccessorAfterAnIrrelevantAppLinkTypeIsRemoved()
    {
        RemotablePluginAccessor firstAccessor = factory.get(PLUGIN_KEY);
        ApplicationLinkDeletedEvent event = mock(ApplicationLinkDeletedEvent.class);
        when(event.getApplicationType()).thenReturn(mock(ApplicationType.class));
        factory.onApplicationLinkRemoved(event);
        assertThat(factory.get(PLUGIN_KEY), is(sameInstance(firstAccessor)));
    }

    @Test
    public void getterReturnsTheSameAccessorAfterAnAppLinkForADifferentPluginIsRemoved()
    {
        RemotablePluginAccessor firstAccessor = factory.get(PLUGIN_KEY);
        removePluginAppLink("different plugin key");
        assertThat(factory.get(PLUGIN_KEY), is(sameInstance(firstAccessor)));
    }

    @Test
    public void getterReturnsADifferentAccessorAfterTheAppLinkForThisPluginIsRemoved()
    {
        RemotablePluginAccessor firstAccessor = factory.get(PLUGIN_KEY);
        removePluginAppLink(PLUGIN_KEY);
        assertThat(factory.get(PLUGIN_KEY), is(not(sameInstance(firstAccessor))));
    }

    @Test
    public void getterReturnsNewAccessorAfterPluginIsEnabled()
    {
        RemotablePluginAccessor firstAccessor = factory.get(PLUGIN_KEY);
        enablePlugin(PLUGIN_KEY);
        assertThat(factory.get(PLUGIN_KEY), is(not(sameInstance(firstAccessor))));
    }

    @Test
    public void getterReturnsSameAccessorAfterADifferentPluginIsEnabled()
    {
        RemotablePluginAccessor firstAccessor = factory.get(PLUGIN_KEY);
        enablePlugin("different plugin key");
        assertThat(factory.get(PLUGIN_KEY), is(sameInstance(firstAccessor)));
    }

    @Test
    public void getterReturnsNewAccessorAfterPluginIsDisabled()
    {
        RemotablePluginAccessor firstAccessor = factory.get(PLUGIN_KEY);
        disablePlugin(PLUGIN_KEY);
        assertThat(factory.get(PLUGIN_KEY), is(not(sameInstance(firstAccessor))));
    }

    @Test
    public void getterReturnsSameAccessorAfterADifferentPluginIsDisabled()
    {
        RemotablePluginAccessor firstAccessor = factory.get(PLUGIN_KEY);
        disablePlugin("different plugin key");
        assertThat(factory.get(PLUGIN_KEY), is(sameInstance(firstAccessor)));
    }

    private Plugin mockPlugin()
    {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getName()).thenReturn(PLUGIN_NAME);
        return plugin;
    }

    private CachingHttpContentRetriever mockCachingHttpContentRetriever()
    {
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

    private void removePluginAppLink(String pluginKey)
    {
        ApplicationLinkDeletedEvent event = mock(ApplicationLinkDeletedEvent.class);
        when(event.getApplicationType()).thenReturn(mock(RemotePluginContainerApplicationType.class));
        ApplicationLink appLink = mock(ApplicationLink.class);
        when(appLink.getProperty(RemotePluginContainerModuleDescriptor.PLUGIN_KEY_PROPERTY)).thenReturn(pluginKey);
        when(event.getApplicationLink()).thenReturn(appLink);
        factory.onApplicationLinkRemoved(event);
    }

    private void enablePlugin(String pluginKey)
    {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn(pluginKey);
        PluginEnabledEvent event = mock(PluginEnabledEvent.class);
        when(event.getPlugin()).thenReturn(plugin);
        factory.onPluginEnabled(event);
    }

    private void disablePlugin(String pluginKey)
    {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn(pluginKey);
        PluginDisabledEvent event = mock(PluginDisabledEvent.class);
        when(event.getPlugin()).thenReturn(plugin);
        factory.onPluginDisabled(event);
    }

    private RemotablePluginAccessor createRemotePluginAccessor()
    {
        return factory.create(PLUGIN_KEY, new Supplier<URI>()
        {
            @Override
            public URI get()
            {
                return URI.create(DISPLAY_URL);
            }
        });
    }
}
