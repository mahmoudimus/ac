package it.com.atlassian.plugin.connect.testlifecycle;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.testlifecycle.util.LifecyclePluginInstaller;
import it.com.atlassian.plugin.connect.testlifecycle.util.LifecycleTestAuthenticator;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class ModuleProviderPluginLifecycleTest extends AbstractPluginLifecycleTest
{

    private static final String ADDON_DESCRIPTOR = "descriptorWithPluginProvidedModule.json";

    private static final Logger log = LoggerFactory.getLogger(AbstractPluginLifecycleTest.class);

    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;

    private Plugin jiraReferencePlugin;
    private Plugin addon;

    public ModuleProviderPluginLifecycleTest(LifecyclePluginInstaller testPluginInstaller,
            LifecycleTestAuthenticator testAuthenticator,
            PluginController pluginController,
            PluginAccessor pluginAccessor)
    {
        super(testAuthenticator, testPluginInstaller);
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
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


        if (null != jiraReferencePlugin)
        {
            try
            {
                testPluginInstaller.uninstallPlugin(jiraReferencePlugin);
            }
            catch (Exception e)
            {
                log.warn("Failed to uninstall plugin", e);
            }
            finally
            {
                jiraReferencePlugin = null;
            }
        }
    }
    @Test
    public void shouldBadThingsHappenWhenConnectReenabled() throws Exception
    {
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        jiraReferencePlugin = testPluginInstaller.installJiraReferencePlugin();
        addon = installAndEnableAddon(ADDON_DESCRIPTOR);
        pluginController.disablePlugin(jiraReferencePlugin.getKey());
        pluginController.disablePlugin(theConnectPlugin.getKey());

        try
        {
            pluginController.enablePlugins(theConnectPlugin.getKey());
        }
        catch (Exception e)
        {
            assertEquals("com.atlassian.plugin.connect.plugin.descriptor", e.getClass().getCanonicalName());
            assertEquals("No provider found for module type jiraTestModules referenced in the descriptor", e.getMessage());
        }
    }
}
