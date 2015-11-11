package it.com.atlassian.plugin.connect.testlifecycle;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import it.com.atlassian.plugin.connect.testlifecycle.util.LifecyclePluginInstaller;
import it.com.atlassian.plugin.connect.testlifecycle.util.LifecycleTestAuthenticator;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.testutils.annotations.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class ConnectPluginLifecycleTest extends AbstractPluginLifecycleTest
{

    public static final String SINGLE_MODULE_ADDON = "single-module.json";
    public static final String DOUBLE_MODULE_ADDON = "double-module.json";

    private static final Logger log = LoggerFactory.getLogger(ConnectPluginLifecycleTest.class);

    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;

    private Plugin theConnectPlugin;
    private Plugin singleModuleAddon;
    private Plugin doubleModuleAddon;

    public ConnectPluginLifecycleTest(LifecyclePluginInstaller testPluginInstaller,
            LifecycleTestAuthenticator testAuthenticator,
            PluginController pluginController,
            PluginAccessor pluginAccessor)
    {
        super(testAuthenticator, testPluginInstaller);
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
    }

    @BeforeClass
    public void setupBeforeAllTests()
    {
        testAuthenticator.authenticateUser("admin");
    }

    @After
    public void tearDown()
    {
        if (null != singleModuleAddon)
        {
            try
            {
                testPluginInstaller.uninstallPlugin(singleModuleAddon);
            }
            catch (Exception e)
            {
                log.warn("Failed to uninstall add-on", e);
            }
            finally
            {
                singleModuleAddon = null;
            }
        }

        if (null != doubleModuleAddon)
        {
            try
            {
                testPluginInstaller.uninstallPlugin(doubleModuleAddon);
            }
            catch (Exception e)
            {
                log.warn("Failed to uninstall add-on", e);
            }
            finally
            {
                doubleModuleAddon = null;
            }
        }
    }

    @Test
    @Retry(maxAttempts=AbstractPluginLifecycleTest.MAX_RETRY_ATTEMPTS)
    public void installConnectSucceeds() throws Exception
    {
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        assertEquals(PluginState.ENABLED, theConnectPlugin.getPluginState());
    }

    @Test
    @Retry(maxAttempts=AbstractPluginLifecycleTest.MAX_RETRY_ATTEMPTS)
    public void installConnectWithAddonSucceeds() throws Exception
    {
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        assertEquals("first enabled check failed", PluginState.ENABLED, theConnectPlugin.getPluginState());

        singleModuleAddon = installAndEnableAddon(SINGLE_MODULE_ADDON);
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "second check");
    }

    @Test
    @Retry(maxAttempts=AbstractPluginLifecycleTest.MAX_RETRY_ATTEMPTS)
    public void disablingAddonSucceeds() throws Exception
    {
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        singleModuleAddon = installAndEnableAddon(SINGLE_MODULE_ADDON);
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");

        testPluginInstaller.disableAddon(singleModuleAddon.getKey());
        assertStateAndModuleCount(singleModuleAddon, PluginState.DISABLED, 0, "second check");
    }

    @Test
    @Retry(maxAttempts=AbstractPluginLifecycleTest.MAX_RETRY_ATTEMPTS)
    public void disablingThenEnablingAddonSucceeds() throws Exception
    {
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        singleModuleAddon = installAndEnableAddon(SINGLE_MODULE_ADDON);
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");

        testPluginInstaller.disableAddon(singleModuleAddon.getKey());
        assertStateAndModuleCount(singleModuleAddon, PluginState.DISABLED, 0, "second check");

        testPluginInstaller.enableAddon(singleModuleAddon.getKey());
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "third check");
    }

    private Collection<ModuleDescriptor<?>> getEnabledModules(Plugin theConnectPlugin)
    {
        return Collections2.filter(theConnectPlugin.getModuleDescriptors(), new Predicate<ModuleDescriptor<?>>()
        {
            @Override
            public boolean apply(@Nullable ModuleDescriptor<?> input)
            {
                return (null != input) ? pluginAccessor.isPluginModuleEnabled(input.getCompleteKey()) : false;
            }
        });
    }

    @Test
    @Retry(maxAttempts=AbstractPluginLifecycleTest.MAX_RETRY_ATTEMPTS)
    public void disablingThenEnablingConnectDisablesAndEnablesAddon() throws Exception
    {
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        singleModuleAddon = installAndEnableAddon(SINGLE_MODULE_ADDON);
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");

        pluginController.disablePlugin(theConnectPlugin.getKey());
        assertEquals(PluginState.DISABLED, theConnectPlugin.getPluginState());
        assertEquals(0, getEnabledModules(theConnectPlugin).size());

        pluginController.enablePlugins(theConnectPlugin.getKey());
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "third check");
    }

    @Test
    @Retry(maxAttempts=AbstractPluginLifecycleTest.MAX_RETRY_ATTEMPTS)
    public void uninstallingAndThenInstallingConnectDisablesAndEnablesAddon() throws Exception
    {
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        singleModuleAddon = installAndEnableAddon(SINGLE_MODULE_ADDON);
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");

        pluginController.uninstall(theConnectPlugin);
        theConnectPlugin = null;
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "third check");
    }

    @Test
    @Retry(maxAttempts=AbstractPluginLifecycleTest.MAX_RETRY_ATTEMPTS)
    public void upgradingConnectEnablesAddon() throws Exception
    {
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        singleModuleAddon = installAndEnableAddon(SINGLE_MODULE_ADDON);
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");

        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "second check");
    }

    @Test
    @Retry(maxAttempts=AbstractPluginLifecycleTest.MAX_RETRY_ATTEMPTS)
    public void disablingThenEnablingConnectDisablesAndEnablesMultipleAddons() throws Exception
    {
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        singleModuleAddon = installAndEnableAddon(SINGLE_MODULE_ADDON);
        doubleModuleAddon = installAndEnableAddon(DOUBLE_MODULE_ADDON);
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "first check");

        pluginController.disablePlugin(theConnectPlugin.getKey());
        assertEquals(PluginState.DISABLED, theConnectPlugin.getPluginState());
        assertEquals(0, getEnabledModules(theConnectPlugin).size());

        pluginController.enablePlugins(theConnectPlugin.getKey());
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "third check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "third check");
    }

    @Test
    @Retry(maxAttempts=AbstractPluginLifecycleTest.MAX_RETRY_ATTEMPTS)
    public void uninstallingAndThenInstallingConnectDisablesAndEnablesMultipleAddons() throws Exception
    {
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        singleModuleAddon = installAndEnableAddon(SINGLE_MODULE_ADDON);
        doubleModuleAddon = installAndEnableAddon(DOUBLE_MODULE_ADDON);
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "first check");

        pluginController.uninstall(theConnectPlugin);
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "third check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "third check");
    }

    @Test
    @Retry(maxAttempts=AbstractPluginLifecycleTest.MAX_RETRY_ATTEMPTS)
    public void upgradingConnectEnablesMultipleAddons() throws Exception
    {
        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        singleModuleAddon = installAndEnableAddon(SINGLE_MODULE_ADDON);
        doubleModuleAddon = installAndEnableAddon(DOUBLE_MODULE_ADDON);
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "first check");

        theConnectPlugin = testPluginInstaller.installConnectPlugin();
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "second check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "second check");
    }
}
