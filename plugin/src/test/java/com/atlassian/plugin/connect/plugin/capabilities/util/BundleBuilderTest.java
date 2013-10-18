package com.atlassian.plugin.connect.plugin.capabilities.util;

import java.io.File;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectPluginXmlFactory;
import com.atlassian.plugin.connect.plugin.installer.RemotePluginArtifactFactory;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.felix.framework.util.MapToDictionary;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since version
 */
public class BundleBuilderTest
{
    RemotePluginArtifactFactory artifactFactory;
    
    @Before
    public void setup() throws Exception
    {
        ConnectPluginXmlFactory xmlFactory = new ConnectPluginXmlFactory();
        BundleContext bc = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);

        Map<String,String> headerMap = new HashMap<String, String>();
        headerMap.put(Constants.IMPORT_PACKAGE, Files.toString(new File(getClass().getResource("/test-import-packages.txt").getFile()), Charsets.UTF_8));
        Dictionary headers = new MapToDictionary(headerMap);
        
        when(bc.getBundle()).thenReturn(bundle);
        when(bundle.getHeaders()).thenReturn(headers);

        ContainerManagedPlugin plugin = mock(ContainerManagedPlugin.class);
        when(plugin.getResourceAsStream(anyString())).thenReturn(getClass().getResourceAsStream("/test-import-packages.txt"));
        PluginRetrievalService pluginRetrievalService = mock(PluginRetrievalService.class);
        when(pluginRetrievalService.getPlugin()).thenReturn(plugin);

        ApplicationProperties appProps = mock(ApplicationProperties.class);
        when(appProps.getDisplayName()).thenReturn("jira");
        
        artifactFactory = new RemotePluginArtifactFactory(xmlFactory,bc,pluginRetrievalService, appProps);
    }
    
    @Test
    public void verifyBundleCreation() throws Exception
    {
        ConnectAddonBean addon = newConnectAddonBean()
                .withKey("my-plugin")
                .withName("my plugin")
                .withVersion("1.0")
                .withCapabilities(newWebItemBean()
                        .withName(new I18nProperty("AC General Web Item", "ac.gen"))
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withLink("/irwi")
                        .build())
                .build();
        
        
        PluginArtifact artifact = artifactFactory.create(addon, "admin");
        assertNotNull(artifact);
    }
    
}
