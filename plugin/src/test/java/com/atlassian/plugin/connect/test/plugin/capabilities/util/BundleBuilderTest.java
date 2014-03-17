package com.atlassian.plugin.connect.test.plugin.capabilities.util;

import java.io.File;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.installer.RemotePluginArtifactFactory;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.felix.framework.util.MapToDictionary;
import org.junit.Before;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since 1.0
 */
public class BundleBuilderTest
{
    RemotePluginArtifactFactory artifactFactory;

    @Before
    public void setup() throws Exception
    {
        BundleContext bc = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);

        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put(Constants.IMPORT_PACKAGE, Files.toString(new File(getClass().getResource("/test-import-packages.txt").getFile()), Charsets.UTF_8));
        Dictionary headers = new MapToDictionary(headerMap);

        when(bc.getBundle()).thenReturn(bundle);
        when(bundle.getHeaders()).thenReturn(headers);

        ContainerManagedPlugin plugin = mock(ContainerManagedPlugin.class);
        when(plugin.getResourceAsStream(anyString())).thenReturn(getClass().getResourceAsStream("/test-import-packages.txt"));
        PluginRetrievalService pluginRetrievalService = mock(PluginRetrievalService.class);
        when(pluginRetrievalService.getPlugin()).thenReturn(plugin);

        artifactFactory = new RemotePluginArtifactFactory();
    }

}
