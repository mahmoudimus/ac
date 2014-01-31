package com.atlassian.plugin.connect.test.plugin;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.capabilities.event.ConnectEventHandler;
import com.atlassian.plugin.connect.plugin.event.RemoteEventsHandler;
import com.atlassian.plugin.connect.plugin.installer.ConnectDescriptorRegistry;
import com.atlassian.plugin.connect.plugin.installer.DefaultConnectAddOnInstaller;
import com.atlassian.plugin.connect.plugin.installer.RemotePluginArtifactFactory;
import com.atlassian.plugin.connect.plugin.installer.SharedSecretServiceImpl;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.UUID;

import static com.atlassian.plugin.connect.test.plugin.util.ConnectInstallationTestUtil.createBean;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultConnectAddOnInstallerJwtTest
{
    private static final String ADD_ON_KEY = "add-on key";
    private static final String BASE_URL = "/baseUrl";
    private static final AuthenticationType AUTHENTICATION_TYPE = AuthenticationType.JWT;
    private static final String PUBLIC_KEY = "public key";
    private static final ConnectAddonBean ADD_ON_BEAN = createBean(AUTHENTICATION_TYPE, PUBLIC_KEY, BASE_URL);
    private static final String ADD_ON_USER_KEY = "the_add_on_user_key";

    private @Mock RemotePluginArtifactFactory remotePluginArtifactFactory;
    private @Mock PluginController pluginController;
    private @Mock PluginAccessor pluginAccessor;
    private @Mock
    OAuthLinkManager oAuthLinkManager;
    private @Mock RemoteEventsHandler remoteEventsHandler;
    private @Mock BeanToModuleRegistrar beanToModuleRegistrar;
    private @Mock BundleContext bundleContext;
    private @Mock ConnectApplinkManager connectApplinkManager;
    private @Mock ConnectDescriptorRegistry connectDescriptorRegistry;
    private @Mock ConnectEventHandler connectEventHandler;
    private @Mock ApplicationService applicationService;
    private @Mock ApplicationManager applicationManager;
    private @Mock Application application;

    private @Mock Plugin plugin;
    private @Mock User addOnUser;

    @Test
    public void appLinkIsCreatedWithCorrectParameters()
    {
        verify(connectApplinkManager).createAppLink(eq(plugin), eq(BASE_URL), eq(AUTHENTICATION_TYPE), argThat(isValidUUID()), eq(ADD_ON_USER_KEY));
    }

    @Test
    public void installEventIsFiredWithCorrectParameters()
    {
        verify(connectEventHandler).pluginInstalled(eq(ADD_ON_BEAN), argThat(isValidUUID()));
    }

    @Before
    public void beforeEachTest() throws InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, ApplicationNotFoundException
    {
        when(pluginController.installPlugins(any(PluginArtifact.class))).thenReturn(Sets.newHashSet(ADD_ON_KEY));
        when(plugin.getKey()).thenReturn(ADD_ON_KEY);
        when(pluginAccessor.getPlugin(ADD_ON_KEY)).thenReturn(plugin);
        when(pluginAccessor.isPluginEnabled(ADD_ON_KEY)).thenReturn(true);
        when(addOnUser.getName()).thenReturn(ADD_ON_USER_KEY);
        when(applicationService.addUser(any(Application.class), any(UserTemplate.class), eq(PasswordCredential.NONE))).thenReturn(addOnUser);
        when(applicationManager.findByName("crowd-embedded")).thenReturn(application);
        new DefaultConnectAddOnInstaller(remotePluginArtifactFactory, pluginController, pluginAccessor, oAuthLinkManager,
                remoteEventsHandler, beanToModuleRegistrar, connectApplinkManager, connectDescriptorRegistry, connectEventHandler, new SharedSecretServiceImpl(),
                applicationService, applicationManager)
            .install("username", ConnectModulesGsonFactory.getGson().toJson(ADD_ON_BEAN));
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
