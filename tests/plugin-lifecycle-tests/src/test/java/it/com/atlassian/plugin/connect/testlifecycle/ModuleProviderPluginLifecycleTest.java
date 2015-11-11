package it.com.atlassian.plugin.connect.testlifecycle;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.testlifecycle.AbstractPluginLifecycleTest;
import it.com.atlassian.plugin.connect.testlifecycle.util.LifecyclePluginInstaller;
import it.com.atlassian.plugin.connect.testlifecycle.util.LifecycleTestAuthenticator;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.testutils.annotations.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;

@RunWith(AtlassianPluginsTestRunner.class)
public class ModuleProviderPluginLifecycleTest extends AbstractPluginLifecycleTest
{

    private static final String ADDON_DESCRIPTOR = "descriptorWithPluginProvidedModule.json";

    private static final Logger log = LoggerFactory.getLogger(AbstractPluginLifecycleTest.class);

    private final PluginController pluginController;

    private Plugin generalReferencePlugin;
    private Plugin addon;

    public ModuleProviderPluginLifecycleTest(LifecyclePluginInstaller testPluginInstaller,
            LifecycleTestAuthenticator testAuthenticator,
            PluginController pluginController)
    {
        super(testAuthenticator, testPluginInstaller);
        this.pluginController = pluginController;
    }

    @After
    public void tearDown()
    {
        if (null != addon)
        {
            try
            {
                testPluginInstaller.uninstallPlugin(addon);
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

        if (null != generalReferencePlugin)
        {
            try
            {
                testPluginInstaller.uninstallPlugin(generalReferencePlugin);
            }
            catch (Exception e)
            {
                log.warn("Failed to uninstall plugin", e);
            }
            finally
            {
                generalReferencePlugin = null;
            }
        }
    }

    @Test
    @Retry(maxAttempts=AbstractPluginLifecycleTest.MAX_RETRY_ATTEMPTS)
    public void shouldSkipAddonEnablementWhenDescriptorValidationFails() throws Exception
    {
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        generalReferencePlugin = testPluginInstaller.installGeneralReferencePlugin();
        addon = installAndEnableAddon(ADDON_DESCRIPTOR);
        pluginController.disablePlugin(generalReferencePlugin.getKey());
        pluginController.disablePlugin(theConnectPlugin.getKey());
        pluginController.enablePlugins(theConnectPlugin.getKey());
        assertFalse(testPluginInstaller.isAddonEnabled(addon.getKey()));

        pluginController.enablePlugins(generalReferencePlugin.getKey());
        testPluginInstaller.enableAddon(addon.getKey());
        assertStateAndModuleCount(addon, PluginState.ENABLED, 1, "With module provider plugin enabled");
    }

    @Test
    @Retry(maxAttempts=AbstractPluginLifecycleTest.MAX_RETRY_ATTEMPTS)
    public void shouldSkipAddonEnablementWhenModuleRegistrationFails() throws Exception
    {
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        generalReferencePlugin = testPluginInstaller.installGeneralReferencePlugin();
        addon = installAndEnableAddon(ADDON_DESCRIPTOR);
        testPluginInstaller.disableAddon(addon.getKey());
        pluginController.disablePlugin(generalReferencePlugin.getKey());
        testPluginInstaller.enableAddon(addon.getKey());
        assertStateAndModuleCount(addon, PluginState.DISABLED, 0, "With module provider plugin disabled");

        pluginController.enablePlugins(generalReferencePlugin.getKey());
        testPluginInstaller.enableAddon(addon.getKey());
        assertStateAndModuleCount(addon, PluginState.ENABLED, 1, "With module provider plugin enabled");
    }
}
