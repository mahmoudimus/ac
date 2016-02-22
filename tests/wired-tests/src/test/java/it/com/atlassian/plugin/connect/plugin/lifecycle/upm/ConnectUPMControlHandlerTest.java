package it.com.atlassian.plugin.connect.plugin.lifecycle.upm;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.AddonSettings;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.upm.spi.PluginControlHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

@RunWith(AtlassianPluginsTestRunner.class)
public class ConnectUPMControlHandlerTest
{

    private static final String ADDON_KEY = "some-addon-key";

    private final PluginControlHandler pluginControlHandler;
    private final ConnectAddonRegistry connectAddonRegistry;

    public ConnectUPMControlHandlerTest(PluginControlHandler pluginControlHandler, ConnectAddonRegistry connectAddonRegistry)
    {
        this.pluginControlHandler = pluginControlHandler;
        this.connectAddonRegistry = connectAddonRegistry;
    }

    @Test
    public void shouldReturnAndUninstallAddonWithInvalidDescriptor() throws JsonProcessingException
    {
        storeInstallationWithInvalidDescriptor();
        Plugin plugin = pluginControlHandler.getPlugin(ADDON_KEY);
        assertThat(plugin.getKey(), equalTo(ADDON_KEY));

        pluginControlHandler.uninstall(plugin);

        assertThat(pluginControlHandler.getPlugin(ADDON_KEY), nullValue());
    }

    /**
     * Stores an add-on installation with an invalid descriptor (missing baseUrl field).
     *
     * @throws JsonProcessingException if descriptor serialization fails
     */
    private void storeInstallationWithInvalidDescriptor() throws JsonProcessingException
    {
        AuthenticationType authenticationType = AuthenticationType.NONE;
        ImmutableMap<String, Object> descriptorMap = ImmutableMap.<String, Object>builder()
                .put("key", ADDON_KEY)
                .put("authentication", ImmutableMap.<String, Object>builder().put("type", authenticationType.name()).build())
                .build();
        String descriptor = new ObjectMapper().writeValueAsString(descriptorMap);

        AddonSettings settings = new AddonSettings()
                .setAuth(authenticationType.name())
                .setBaseUrl("https://example.com")
                .setDescriptor(descriptor)
                .setRestartState(PluginState.ENABLED);
        connectAddonRegistry.storeAddonSettings(ADDON_KEY, settings);
    }
}
