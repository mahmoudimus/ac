package com.atlassian.plugin.connect.test.plugin.capabilities;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JsonConnectAddOnIdentifierServiceTest
{
    private JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private @Mock PluginAccessor pluginAccessor;
    private @Mock ConnectAddonRegistry connectAddonRegistry;
    private @Mock Plugin plugin;

    @Test
    public void bogusPluginKeyIsNotConnectAddOn()
    {
        when(pluginAccessor.getPlugin("fubar")).thenReturn(null);
        assertThat(jsonConnectAddOnIdentifierService.isConnectAddOn("fubar"), is(false));
    }

    @Test
    public void pluginWithNoManifestIsNotConnectAddOn()
    {
        when(pluginAccessor.getPlugin("the key")).thenReturn(plugin);
        assertThat(jsonConnectAddOnIdentifierService.isConnectAddOn("the key"), is(false));
    }

    @Test
    public void pluginWithNoManifestConnectHeaderIsNotConnectAddOn()
    {
        InputStream manifest = new ByteArrayInputStream(("Other-header: foo\n").getBytes());
        when(pluginAccessor.getPlugin("the key")).thenReturn(plugin);
        when(plugin.getResourceAsStream("/META-INF/MANIFEST.MF")).thenReturn(manifest);
        assertThat(jsonConnectAddOnIdentifierService.isConnectAddOn("the key"), is(false));
    }

    @Test
    public void connectAddOnIsIdentified() throws IOException
    {
        InputStream manifest = new ByteArrayInputStream((ConnectAddOnIdentifierService.CONNECT_ADDON_HEADER + ": foo\n").getBytes());
        when(pluginAccessor.getPlugin("the key")).thenReturn(plugin);
        when(plugin.getResourceAsStream("/META-INF/MANIFEST.MF")).thenReturn(manifest);
        assertThat(jsonConnectAddOnIdentifierService.isConnectAddOn("the key"), is(true));
    }

    @Before
    public void beforeEachTest()
    {
        jsonConnectAddOnIdentifierService = new JsonConnectAddOnIdentifierService(pluginAccessor, connectAddonRegistry);
    }
}
