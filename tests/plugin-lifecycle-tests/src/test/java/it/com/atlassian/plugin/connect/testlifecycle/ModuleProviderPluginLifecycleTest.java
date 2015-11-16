package it.com.atlassian.plugin.connect.testlifecycle;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.testlifecycle.util.LifecyclePluginHelper;
import it.com.atlassian.plugin.connect.testlifecycle.util.LifecycleUpmHelper;
import it.com.atlassian.plugin.connect.testlifecycle.util.LifecycleTestAuthenticator;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(AtlassianPluginsTestRunner.class)
public class ModuleProviderPluginLifecycleTest extends AbstractPluginLifecycleTest
{

    private static final String ADDON_DESCRIPTOR = "descriptorWithPluginProvidedModule.json";

    private static final Logger log = LoggerFactory.getLogger(AbstractPluginLifecycleTest.class);

    private Plugin referencePlugin;
    private Plugin addon;

    public ModuleProviderPluginLifecycleTest(PluginController pluginController,
            LifecyclePluginHelper pluginHelper,
            LifecycleUpmHelper upmHelper,
            LifecycleTestAuthenticator testAuthenticator)
    {
        super(pluginController, pluginHelper, upmHelper, testAuthenticator);
    }

    @After
    public void tearDown()
    {
        if (null != addon)
        {
            try
            {
                pluginController.uninstall(addon);
            }
            catch (Exception e)
            {
                log.warn("Failed to uninstall add-on", e);
            }
            finally
            {
                addon = null;
            }
        }

        if (null != referencePlugin)
        {
            try
            {
                pluginController.uninstall(referencePlugin);
            }
            catch (Exception e)
            {
                log.warn("Failed to uninstall plugin", e);
            }
            finally
            {
                referencePlugin = null;
            }
        }
    }

    @Test
    public void shouldSkipAddonEnablementWhenDescriptorValidationFails() throws Exception
    {
        theConnectPlugin = pluginHelper.installConnectPlugin();
        referencePlugin = pluginHelper.installGeneralReferencePlugin();
        addon = installAndEnableAddon(ADDON_DESCRIPTOR);
        pluginController.disablePlugin(referencePlugin.getKey());
        pluginController.disablePlugin(theConnectPlugin.getKey());
        pluginController.enablePlugins(theConnectPlugin.getKey());
        assertFalse(upmHelper.getUpmControlHandler().isPluginEnabled(addon.getKey()));

        pluginController.enablePlugins(referencePlugin.getKey());
        upmHelper.getUpmControlHandler().enablePlugins(addon.getKey());
        assertStateAndModuleCount(addon, PluginState.ENABLED, 1, "With module provider plugin enabled");
    }

    @Test
    public void shouldSkipAddonEnablementWhenModuleRegistrationFails() throws Exception
    {
        theConnectPlugin = pluginHelper.installConnectPlugin();
        referencePlugin = pluginHelper.installGeneralReferencePlugin();
        addon = installAndEnableAddon(ADDON_DESCRIPTOR);
        upmHelper.getUpmControlHandler().disablePlugin(addon.getKey());
        pluginController.disablePlugin(referencePlugin.getKey());
        upmHelper.getUpmControlHandler().enablePlugins(addon.getKey());
        assertStateAndModuleCount(addon, PluginState.DISABLED, 0, "With module provider plugin disabled");

        pluginController.enablePlugins(referencePlugin.getKey());
        upmHelper.getUpmControlHandler().enablePlugins(addon.getKey());
        assertStateAndModuleCount(addon, PluginState.ENABLED, 1, "With module provider plugin enabled");
    }

    @Test
    public void shouldReturnPluginToUpmWhenDescriptorValidationFails() throws Exception
    {
        theConnectPlugin = pluginHelper.installConnectPlugin();
        referencePlugin = pluginHelper.installGeneralReferencePlugin();
        addon = installAndEnableAddon(ADDON_DESCRIPTOR);
        pluginController.disablePlugin(referencePlugin.getKey());
        pluginController.disablePlugin(theConnectPlugin.getKey());
        pluginController.enablePlugins(theConnectPlugin.getKey());
        assertFalse(upmHelper.getUpmControlHandler().isPluginEnabled(addon.getKey()));

        Plugin plugin = upmHelper.getUpmControlHandler().getPlugin(addon.getKey());
        assertNotNull(plugin);
    }
}
