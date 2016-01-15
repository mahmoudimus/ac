package com.atlassian.plugin.connect.plugin.web.dialog;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.DialogModuleBean;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DialogModuleProviderTest
{
    @InjectMocks
    private DialogModuleProvider provider;

    @Mock
    private PluginRetrievalService pluginRetrievalService;

    @Mock
    private ConnectAddonBean addon;

    @Mock
    private DialogModuleBean module;

    @Mock
    private Plugin plugin;

    @Before
    public void setUp()
    {
    }

    @Test
    public void moduleDescriptorShouldIncludeDialogOptions()
    {
        String moduleKey = "the-key";
        when(module.getKey(addon)).thenReturn(moduleKey);

        when(pluginRetrievalService.getPlugin()).thenReturn(plugin);

        List<ModuleDescriptor> descriptors = provider.createPluginModuleDescriptors(ImmutableList.of(module), addon);

        // Mock the DialogModuleDescriptor so that we don't have to think about that init() method.
        ModuleDescriptor expectedDescriptor = mock(DialogModuleDescriptor.class);
        when(expectedDescriptor.getKey()).thenReturn(moduleKey);

        assertThat(descriptors, hasSize(1));

        ModuleDescriptor actualDescriptor = descriptors.get(0);
        assertThat(actualDescriptor.getKey(), is(expectedDescriptor.getKey()));
    }
}
