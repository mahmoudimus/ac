package com.atlassian.plugin.connect.plugin;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.event.ConnectEventHandler;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.event.RemoteEventsHandler;
import com.atlassian.plugin.connect.plugin.installer.ConnectDescriptorRegistry;
import com.atlassian.plugin.connect.plugin.installer.DefaultConnectAddOnInstaller;
import com.atlassian.plugin.connect.plugin.installer.RemotePluginArtifactFactory;
import com.atlassian.plugin.connect.plugin.installer.SharedSecretServiceImpl;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static com.atlassian.plugin.connect.plugin.util.ConnectInstallationTestUtil.createBean;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultConnectAddOnInstallerOAuthTest
{
    private static final String ADD_ON_KEY = "add-on key";
    private static final String BASE_URL = "/baseUrl";
    private static final AuthenticationType AUTHENTICATION_TYPE = AuthenticationType.OAUTH;
    private static final String PUBLIC_KEY = "public key";
    private static final ConnectAddonBean ADD_ON_BEAN = createBean(AUTHENTICATION_TYPE, PUBLIC_KEY, BASE_URL);

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

    @Test
    public void appLinkIsCreatedWithCorrectParameters()
    {
        verify(connectApplinkManager).createAppLink(eq(plugin), eq(BASE_URL), eq(AUTHENTICATION_TYPE), eq(PUBLIC_KEY));
    }

    @Test
    public void installEventIsFiredWithCorrectParameters()
    {
        verify(connectEventHandler).pluginInstalled(eq(ADD_ON_BEAN), isNull(String.class));
    }

    @Before
    public void beforeEachTest()
    {
        when(pluginController.installPlugins(any(PluginArtifact.class))).thenReturn(Sets.newHashSet(ADD_ON_KEY));
        when(plugin.getKey()).thenReturn(ADD_ON_KEY);
        when(pluginAccessor.getPlugin(ADD_ON_KEY)).thenReturn(plugin);
        when(pluginAccessor.isPluginEnabled(ADD_ON_KEY)).thenReturn(true);
        new DefaultConnectAddOnInstaller(remotePluginArtifactFactory, pluginController, pluginAccessor, oAuthLinkManager,
                remoteEventsHandler, beanToModuleRegistrar, connectApplinkManager, connectDescriptorRegistry, connectEventHandler, new SharedSecretServiceImpl())
            .install("username", ConnectModulesGsonFactory.getGson().toJson(ADD_ON_BEAN));
    }
}
