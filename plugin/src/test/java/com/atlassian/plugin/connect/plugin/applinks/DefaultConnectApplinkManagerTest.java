package com.atlassian.plugin.connect.plugin.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.application.generic.GenericApplicationType;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutableApplicationLink;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.module.applinks.RemotePluginContainerApplicationTypeImpl;
import com.atlassian.plugin.connect.spi.AuthenticationMethod;
import com.atlassian.plugin.connect.spi.applinks.RemotePluginContainerApplicationType;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultConnectApplinkManagerTest
{
    @Mock private MutatingApplicationLinkService applicationLinkService;
    @Mock private TypeAccessor typeAccessor;
    @Mock private PluginSettingsFactory pluginSettingsFactory;
    @Mock private OAuthLinkManager oAuthLinkManager;
    @Mock private PermissionManager permissionManager;
    private TransactionTemplate transactionTemplate;

    private ConnectApplinkManager connectApplinkManager;
    @Mock private RemotePluginContainerApplicationType connectApplicationType;
    @Mock private JiraApplicationType jiraApplicationType;
    @Mock private GenericApplicationType genericApplicationType;

    @Before
    public void setup()
    {
        transactionTemplate = new TransactionTemplate()
        {
            @Override
            public <T> T execute(TransactionCallback<T> action)
            {
                return action.doInTransaction();
            }
        };

        connectApplinkManager = new DefaultConnectApplinkManager(applicationLinkService, typeAccessor,
                        pluginSettingsFactory, oAuthLinkManager, permissionManager, transactionTemplate);

        when(connectApplicationType.getId()).thenReturn(RemotePluginContainerApplicationTypeImpl.TYPE_ID);
    }

    @Test
    public void testGetAppLinkForConnectAddon() throws Exception
    {
        ApplicationLink link = mock(ApplicationLink.class);
        String addonKey = "my-connect-addon";
        when(link.getProperty(DefaultConnectApplinkManager.PLUGIN_KEY_PROPERTY)).thenReturn(addonKey);
        when(link.getType()).thenReturn(connectApplicationType);

        List<ApplicationLink> links = Lists.newArrayList(link);

        when(applicationLinkService.getApplicationLinks()).thenReturn(links);

        assertNotNull("Application link should be returned", connectApplinkManager.getAppLink(addonKey));
    }

    @Test
    public void testGetAppLinkForConnectAddonWithOtherApplinks() throws Exception
    {
        ApplicationLink connectLink = mock(ApplicationLink.class);
        String connectKey = "my-connect-addon";
        when(connectLink.getProperty(DefaultConnectApplinkManager.PLUGIN_KEY_PROPERTY)).thenReturn(connectKey);
        when(connectLink.getType()).thenReturn(connectApplicationType);

        ApplicationLink jiraLink = mock(ApplicationLink.class);
        String jiraKey = "a-jira-plugin";
        when(jiraLink.getProperty(DefaultConnectApplinkManager.PLUGIN_KEY_PROPERTY)).thenReturn(jiraKey);
        when(jiraLink.getType()).thenReturn(jiraApplicationType);

        ApplicationLink genericLink = mock(ApplicationLink.class);
        when(genericLink.getProperty(DefaultConnectApplinkManager.PLUGIN_KEY_PROPERTY)).thenReturn(null);
        when(genericLink.getType()).thenReturn(genericApplicationType);

        List<ApplicationLink> links = Lists.newArrayList(connectLink, jiraLink, genericLink);

        when(applicationLinkService.getApplicationLinks()).thenReturn(links);

        assertNotNull("Application link should be returned", connectApplinkManager.getAppLink(connectKey));
    }

    @Test(expected = NotConnectAddonException.class)
    public void testGetAppLinkForNonConnectAddonThrowsException() throws Exception
    {
        ApplicationLink connectLink = mock(ApplicationLink.class);
        String connectKey = "my-connect-addon";
        when(connectLink.getProperty(DefaultConnectApplinkManager.PLUGIN_KEY_PROPERTY)).thenReturn(connectKey);
        when(connectLink.getType()).thenReturn(connectApplicationType);

        ApplicationLink jiraLink = mock(ApplicationLink.class);
        String jiraKey = "a-jira-plugin";
        when(jiraLink.getProperty(DefaultConnectApplinkManager.PLUGIN_KEY_PROPERTY)).thenReturn(jiraKey);
        when(jiraLink.getType()).thenReturn(jiraApplicationType);

        ApplicationLink genericLink = mock(ApplicationLink.class);
        when(genericLink.getProperty(DefaultConnectApplinkManager.PLUGIN_KEY_PROPERTY)).thenReturn(null);
        when(genericLink.getType()).thenReturn(genericApplicationType);

        List<ApplicationLink> links = Lists.newArrayList(connectLink, jiraLink, genericLink);

        when(applicationLinkService.getApplicationLinks()).thenReturn(links);

        connectApplinkManager.getAppLink(jiraKey);
    }

    @Test
    public void creatingAnAddOnWithJwtAuthenticationSetsAuthenticationMethod()
    {
        MutableApplicationLink appLink = mock(MutableApplicationLink.class);
        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn("my-connect-addon");
        when(applicationLinkService.addApplicationLink(any(ApplicationId.class), any(ApplicationType.class), any(ApplicationLinkDetails.class))).thenReturn(appLink);
        when(applicationLinkService.getApplicationLinks(RemotePluginContainerApplicationType.class)).thenReturn(Collections.<ApplicationLink>emptyList());
        connectApplinkManager.createAppLink(plugin, "/baseUrl", AuthenticationType.JWT, "signing key");
        verify(appLink).putProperty(AuthenticationMethod.PROPERTY_NAME, AuthenticationMethod.JWT);
    }

    @Test
    public void creatingAnAddOnWithJwtAuthenticationSetsTheSharedSecret()
    {
        MutableApplicationLink appLink = mock(MutableApplicationLink.class);
        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn("my-connect-addon");
        when(applicationLinkService.addApplicationLink(any(ApplicationId.class), any(ApplicationType.class), any(ApplicationLinkDetails.class))).thenReturn(appLink);
        when(applicationLinkService.getApplicationLinks(RemotePluginContainerApplicationType.class)).thenReturn(Collections.<ApplicationLink>emptyList());
        connectApplinkManager.createAppLink(plugin, "/baseUrl", AuthenticationType.JWT, "signing key");
        verify(appLink).putProperty("atlassian.jwt.shared.secret", "signing key");
    }
}
