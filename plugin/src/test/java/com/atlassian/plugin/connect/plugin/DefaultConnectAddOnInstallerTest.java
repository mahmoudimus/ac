package com.atlassian.plugin.connect.plugin;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.capabilities.beans.*;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.event.ConnectEventHandler;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.atlassian.plugin.connect.plugin.event.RemoteEventsHandler;
import com.atlassian.plugin.connect.plugin.installer.ConnectDescriptorRegistry;
import com.atlassian.plugin.connect.plugin.installer.DefaultConnectAddOnInstaller;
import com.atlassian.plugin.connect.plugin.installer.RemotePluginArtifactFactory;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.UUID;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultConnectAddOnInstallerTest
{
    private static final String ADD_ON_KEY = "add-on key";
    private static final String BASE_URL = "/baseUrl";
    private static final AuthenticationType AUTHENTICATION_TYPE = AuthenticationType.JWT;
    private static final String PUBLIC_KEY = "public key";
    private static final ConnectAddonBean ADD_ON_BEAN = ConnectAddonBean.newConnectAddonBean()
            .withAuthentication(AuthenticationBean.newAuthenticationBean()
                    .withSharedKey(PUBLIC_KEY)
                    .withType(AUTHENTICATION_TYPE)
                    .build())
            .withBaseurl(BASE_URL)
            .withCapability("webItems", WebItemCapabilityBean.newWebItemBean()
                    .withLink("/webItem")
                    .withLocation("location")
                    .withName(new I18nProperty("text", "key")) // leaving this out results in a null vs empty-string mismatch between original and serialized-then-deserialized beans
                    .withTooltip(new I18nProperty("text", "key")) // leaving this out results in a null vs empty-string mismatch between original and serialized-then-deserialized beans
                    .withIcon(IconBean.newIconBean()
                        .withWidth(16)
                        .withHeight(16)
                        .withUrl("/icon") // leaving this out results in a null vs empty-string mismatch between original and serialized-then-deserialized beans
                        .build())
                    .build())
            .withDescription("description")
            .withKey(ADD_ON_KEY)
            .withLifecycle(LifecycleBean.newLifecycleBean()
                    .withInstalled("/installed")
                    .build())
            .withName("name")
            .build();

    private @Mock RemotePluginArtifactFactory remotePluginArtifactFactory;
    private @Mock PluginController pluginController;
    private @Mock PluginAccessor pluginAccessor;
    private @Mock OAuthLinkManager oAuthLinkManager;
    private @Mock RemoteEventsHandler remoteEventsHandler;
    private @Mock BeanToModuleRegistrar beanToModuleRegistrar;
    private @Mock BundleContext bundleContext;
    private @Mock ConnectApplinkManager connectApplinkManager;
    private @Mock ConnectDescriptorRegistry connectDescriptorRegistry;
    private @Mock ConnectEventHandler connectEventHandler;

    private @Mock Plugin plugin;

    @Before
    public void beforeEachTest()
    {
        when(pluginController.installPlugins(any(PluginArtifact.class))).thenReturn(Sets.newHashSet(ADD_ON_KEY));
        when(plugin.getKey()).thenReturn(ADD_ON_KEY);
        when(pluginAccessor.getPlugin(ADD_ON_KEY)).thenReturn(plugin);
        when(pluginAccessor.isPluginEnabled(ADD_ON_KEY)).thenReturn(true);
        new DefaultConnectAddOnInstaller(remotePluginArtifactFactory, pluginController, pluginAccessor, oAuthLinkManager,
                remoteEventsHandler, beanToModuleRegistrar, bundleContext, connectApplinkManager, connectDescriptorRegistry, connectEventHandler)
            .install("username", CapabilitiesGsonFactory.getGson(bundleContext).toJson(ADD_ON_BEAN));
    }

    @Test
    public void appLinkIsCreatedWithCorrectParameters()
    {
        verify(connectApplinkManager).createAppLink(eq(plugin), eq(BASE_URL), eq(AUTHENTICATION_TYPE), argThat(isValidUUID()));
    }

    @Test
    public void installEventIsFiredWithCorrectParameters()
    {
        verify(connectEventHandler).pluginInstalled(eq(ADD_ON_BEAN), argThat(isValidUUID()));
    }

    private ArgumentMatcher<String> isValidUUID()
    {
        return new ArgumentMatcher<String>()
        {
            @Override
            public boolean matches(Object actual)
            {
                return actual instanceof String && !PUBLIC_KEY.equals(actual) && UUID.fromString((String)actual) != null; // UUID.fromString() will object if the format is not UUID
            }
        };
    }
}
