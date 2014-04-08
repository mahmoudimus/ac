package com.atlassian.plugin.connect.test.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.event.ApplicationLinkAddedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkDeletedEvent;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.*;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.applinks.DefaultConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.util.http.CachingHttpContentRetriever;
import com.atlassian.plugin.connect.spi.AuthenticationMethod;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.applinks.RemotePluginContainerApplicationType;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Supplier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ConvertToWiredTest
@Ignore("convert to wired test")
@RunWith(MockitoJUnitRunner.class)
public class DefaultRemotablePluginAccessorFactoryTest
{
    private static final String PLUGIN_KEY = "plugin key";
    private static final String PLUGIN_NAME = "plugin name";
    public static final String BASE_URL = "http://server:1234/contextPath";

    @Mock private ConnectApplinkManager connectApplinkManager;
    @Mock private ConnectAddonRegistry descriptorRegistry;
    @Mock private PluginAccessor pluginAccessor;
    @Mock private ApplicationProperties applicationProperties;
    @Mock private EventPublisher eventPublisher;
    @Mock private ConnectAddOnIdentifierService connectIdentifier;
    @Mock private OAuthLinkManager oAuthLinkManager;
    @Mock private PluginRetrievalService pluginRetrievalService;
    @Mock private JwtService jwtService;
    @Mock private ConsumerService consumerService;
    @Mock private UserManager userManager;
    @Mock private ConnectAddonBeanFactory connectAddonBeanFactory;

    private DefaultRemotablePluginAccessorFactory factory;

    private Plugin plugin;

    @Before
    public void beforeEachTest() throws ExecutionException, InterruptedException
    {
        this.plugin = mockPlugin();
        when(pluginAccessor.getPlugin(PLUGIN_KEY)).thenReturn(plugin);

        when(connectApplinkManager.getAppLink(PLUGIN_KEY)).thenReturn(mock(ApplicationLink.class));
        factory = new DefaultRemotablePluginAccessorFactory(connectApplinkManager, descriptorRegistry, oAuthLinkManager, mockCachingHttpContentRetriever(), pluginAccessor, applicationProperties, eventPublisher,jwtService, consumerService, userManager, connectAddonBeanFactory);
    }

    @Test
    public void createdRemotePluginAccessorIsNotNull()
    {
        assertThat(createRemotePluginAccessor(), is(notNullValue()));
    }

    @Test
    public void createsOAuthSigningPluginAccessorByDefault()
    {
        when(connectApplinkManager.getAppLink(PLUGIN_KEY)).thenReturn(mock(ApplicationLink.class));
        assertThat(factory.create(plugin, PLUGIN_KEY, null), is(instanceOf(OAuthSigningRemotablePluginAccessor.class)));
    }

    @Test
    public void createsNoAuthSigningPluginAccessorWhenRequired()
    {
        ApplicationLink applicationLink = mock(ApplicationLink.class);
        when(connectApplinkManager.getAppLink(PLUGIN_KEY)).thenReturn(applicationLink);
        when(applicationLink.getProperty(AuthenticationMethod.PROPERTY_NAME)).thenReturn(AuthenticationMethod.NONE.toString().toLowerCase());
        assertThat(factory.create(plugin, PLUGIN_KEY, null), is(instanceOf(NoAuthRemotablePluginAccessor.class)));
    }

    @Test
    public void createsOAuthSigningPluginAccessorWhenRequired()
    {
        ApplicationLink applicationLink = mock(ApplicationLink.class);
        when(applicationLink.getProperty(AuthenticationMethod.PROPERTY_NAME)).thenReturn(AuthenticationMethod.OAUTH1.toString().toLowerCase());
        when(connectApplinkManager.getAppLink(PLUGIN_KEY)).thenReturn(applicationLink);
        assertThat(factory.create(plugin, PLUGIN_KEY, null), is(instanceOf(OAuthSigningRemotablePluginAccessor.class)));
    }

    @Test
    public void createsJwtSigningPluginAccessorWhenRequired()
    {
        ApplicationLink applicationLink = mock(ApplicationLink.class);
        when(applicationLink.getProperty(AuthenticationMethod.PROPERTY_NAME)).thenReturn(AuthenticationMethod.JWT.toString().toLowerCase());
        when(connectApplinkManager.getAppLink(PLUGIN_KEY)).thenReturn(applicationLink);
        assertThat(factory.create(plugin, PLUGIN_KEY, null), is(instanceOf(JwtSigningRemotablePluginAccessor.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownSigningMethodResultsInException()
    {
        ApplicationLink applicationLink = mock(ApplicationLink.class);
        when(applicationLink.getProperty(AuthenticationMethod.PROPERTY_NAME)).thenReturn("unknown");
        when(connectApplinkManager.getAppLink(PLUGIN_KEY)).thenReturn(applicationLink);
        factory.create(plugin, PLUGIN_KEY, null);
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
        when(plugin.getKey()).thenReturn(PLUGIN_KEY);
        when(plugin.getName()).thenReturn(PLUGIN_NAME);
        return plugin;
    }

    private CachingHttpContentRetriever mockCachingHttpContentRetriever()
    {
        return new CachingHttpContentRetriever(mock(HttpClientFactory.class, RETURNS_DEEP_STUBS), mock(PluginRetrievalService.class, RETURNS_DEEP_STUBS));
    }

    private void removePluginAppLink(String pluginKey)
    {
        ApplicationLinkDeletedEvent event = mock(ApplicationLinkDeletedEvent.class);
        when(event.getApplicationType()).thenReturn(mock(RemotePluginContainerApplicationType.class));
        ApplicationLink appLink = mock(ApplicationLink.class);
        when(appLink.getProperty(DefaultConnectApplinkManager.PLUGIN_KEY_PROPERTY)).thenReturn(pluginKey);
        when(event.getApplicationLink()).thenReturn(appLink);
        factory.onApplicationLinkRemoved(event);
    }

    private void enablePlugin(String pluginKey)
    {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn(pluginKey);
        PluginEnabledEvent event = mock(PluginEnabledEvent.class);
        when(event.getPlugin()).thenReturn(plugin);
        //factory.onPluginEnabled(event);
    }

    private void disablePlugin(String pluginKey)
    {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn(pluginKey);
        PluginDisabledEvent event = mock(PluginDisabledEvent.class);
        when(event.getPlugin()).thenReturn(plugin);
        //factory.onPluginDisabled(event);
    }

    private RemotablePluginAccessor createRemotePluginAccessor()
    {
        return factory.create(plugin, PLUGIN_KEY, new Supplier<URI>()
        {
            @Override
            public URI get()
            {
                return URI.create(BASE_URL);
            }
        });
    }
}
