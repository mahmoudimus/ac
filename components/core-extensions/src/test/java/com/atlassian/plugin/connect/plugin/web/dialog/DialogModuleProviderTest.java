package com.atlassian.plugin.connect.plugin.web.dialog;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.DialogModuleBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.google.common.collect.ImmutableList.of;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DialogModuleProviderTest
{
    @InjectMocks
    private DialogModuleProvider provider;

    @Mock
    private ConnectAddonBean addon;

    @Mock
    private DialogModuleBean module;

    @Test
    public void providerReturnsEmptyDescriptorList()
    {
        assertThat(provider.createPluginModuleDescriptors(of(module), addon), empty());
    }
}
