package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectPluginXmlFactory;
import com.atlassian.plugin.connect.plugin.event.RemoteEventsHandler;
import com.atlassian.plugin.connect.spi.InstallationFailedException;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Author: pbrownlow
 * Date: 26/08/13
 * Time: 10:55 AM
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultConnectAddOnInstaller
{
    public static final String PLUGIN_KEY = "plugin-key";
    private RemotePluginArtifactFactory remotePluginArtifactFactory;
    private @Mock PluginController pluginController;
    private @Mock PluginAccessor pluginAccessor;
    private @Mock OAuthLinkManager oAuthLinkManager;
    private RemoteEventsHandler remoteEventsHandler = null;
    private Document document;

    private DefaultConnectAddOnInstaller installer;

    @Before
    public void beforeTests() throws DocumentException
    {
        ContainerManagedPlugin plugin = mock(ContainerManagedPlugin.class);
        when(plugin.getResourceAsStream(anyString())).thenReturn(getClass().getResourceAsStream("/test-import-packages.txt"));
        PluginRetrievalService pluginRetrievalService = mock(PluginRetrievalService.class);
        when(pluginRetrievalService.getPlugin()).thenReturn(plugin);

        ApplicationProperties appProps = mock(ApplicationProperties.class);
        when(appProps.getDisplayName()).thenReturn("jira");
        
        this.remotePluginArtifactFactory = new RemotePluginArtifactFactory(new ConnectPluginXmlFactory(), mock(BundleContext.class),pluginRetrievalService);
        // this plugin xml needs to be syntactically valid
        // (and incidentally the duplicate <webhook> elements, while not strictly necessary for this test,
        //  demonstrate how this problem manifests in production)
        document = DocumentHelper.parseText("<?xml version=\"1.0\" ?>\n" +
                "<atlassian-plugin key=\"myaddon_helloworld\" name=\"Hello World\" plugins-version=\"2\">\n" +
                " \n" +
                "    <plugin-info>\n" +
                "        <description>Atlassian Connect add-on</description>\n" +
                "        <version>1</version>\n" +
                "        <vendor name=\"My Organization, Inc\" url=\"https://developer.atlassian.com\" />\n" +
                "    </plugin-info>\n" +
                " \n" +
                "    <remote-plugin-container key=\"container\" display-url=\"http://murphy.dyn.syd.atlassian.com:8000\">\n" +
                "    </remote-plugin-container>\n" +
                "\n" +
                "    <general-page key=\"general\" name=\"AC Builder\" url=\"/helloworld.html\">\n" +
                "    </general-page>\n" +
                "\n" +
                "    <webhook application=\"confluence\" key=\"page_created\" event=\"page_created\" url=\"/confluence/page_created\"/>\n" +
                "    <webhook application=\"confluence\" key=\"page_created\" event=\"page_updated\" url=\"/confluence/page_updated\"/>\n" +
                "\n" +
                "</atlassian-plugin>");
        installer = new DefaultConnectAddOnInstaller(remotePluginArtifactFactory, pluginController, pluginAccessor, oAuthLinkManager, remoteEventsHandler, mock(BeanToModuleRegistrar.class), mock(BundleContext.class), connectApplinkManager, connectDescriptorRegistry, connectEventHandler);
        // the DefaultConnectAddOnInstaller parses the plugin key for an unloadable plugin, which is returned by installPlugins(),
        // but then does not load the unloadable plugin, resulting in getPlugin(key) returning null
        when(pluginController.installPlugins(any(PluginArtifact.class))).thenReturn(new HashSet<String>(Arrays.asList(PLUGIN_KEY)));
        when(pluginAccessor.getPlugin(PLUGIN_KEY)).thenReturn(null);
    }

    @Test(expected = InstallationFailedException.class)
    public void unloadablePluginCausesInstallationFailure()
    {
        installer.install("username", document);
    }

    @Test
    public void installationFailureNotCausedByNullPointer()
    {
        try
        {
            installer.install("username", document);
        }
        catch (InstallationFailedException e)
        {
            Throwable cause = e;

            while (null != (cause = cause.getCause()))
            {
                assertThat(cause, is(not(instanceOf(NullPointerException.class))));
            }
        }
    }
}
