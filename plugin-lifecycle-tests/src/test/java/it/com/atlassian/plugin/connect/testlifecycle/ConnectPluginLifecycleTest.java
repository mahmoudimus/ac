package it.com.atlassian.plugin.connect.testlifecycle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AtlassianPluginsTestRunner.class)
public class ConnectPluginLifecycleTest
{
    public static final String CONNECT_PLUGIN_PATH = "/connect-plugin-path.txt";
    public static final String OLD_CONNECT_PLUGIN_PATH = "/old-connect-plugin-path.txt";
    public static final String JSON_TEMPLATE_PREFIX = "/json/";
    public static final String SINGLE_MODULE_ADDON = "single-module.json";
    public static final String DOUBLE_MODULE_ADDON = "double-module.json";

    private static final String CONNECT_DESCRIPTOR_PREFIX = "ac.desc.";
    private static final String CONNECT_BASEURL_PREFIX = "ac.baseurl.";
    private static final String CONNECT_SECRET_PREFIX = "ac.secret.";
    private static final String CONNECT_USER_PREFIX = "ac.user.";
    private static final String CONNECT_AUTH_PREFIX = "ac.auth.";
    
    private static final List<String> settingKeys = Lists.newArrayList(
            CONNECT_DESCRIPTOR_PREFIX
            ,CONNECT_BASEURL_PREFIX
            ,CONNECT_SECRET_PREFIX
            ,CONNECT_USER_PREFIX
            ,CONNECT_AUTH_PREFIX
    );

    private final LifecyclePluginInstaller testPluginInstaller;
    private final PluginController pluginController;
    private final PluginSettingsFactory pluginSettingsFactory;

    private Plugin theConnectPlugin;
    private Plugin theOLDConnectPlugin;
    private Plugin singleModuleAddon;
    private Plugin doubleModuleAddon;

    public ConnectPluginLifecycleTest(LifecyclePluginInstaller testPluginInstaller, PluginController pluginController, PluginSettingsFactory pluginSettingsFactory)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.pluginController = pluginController;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @After
    public void tearDown()
    {
        if (null != theConnectPlugin)
        {
            try
            {
                testPluginInstaller.uninstallPlugin(theConnectPlugin);
                theConnectPlugin = null;
            }
            catch (Exception e)
            {
                theConnectPlugin = null;
            }
        }

        if (null != theOLDConnectPlugin)
        {
            try
            {
                testPluginInstaller.uninstallPlugin(theOLDConnectPlugin);
                theOLDConnectPlugin = null;
            }
            catch (Exception e)
            {
                theOLDConnectPlugin = null;
            }
        }

        if (null != singleModuleAddon)
        {
            try
            {
                testPluginInstaller.uninstallPlugin(singleModuleAddon);
                singleModuleAddon = null;
            }
            catch (Exception e)
            {
                singleModuleAddon = null;
            }
        }

        if (null != doubleModuleAddon)
        {
            try
            {
                testPluginInstaller.uninstallPlugin(doubleModuleAddon);
                doubleModuleAddon = null;
            }
            catch (Exception e)
            {
                doubleModuleAddon = null;
            }
        }

        clearConnectPluginSettings();
    }
    
    @Test
    public void installConnectSucceeds() throws Exception
    {
        theConnectPlugin = installConnectPlugin();

        assertEquals(PluginState.ENABLED, theConnectPlugin.getPluginState());
    }

    @Test
    public void installConnectWithAddonSucceeds() throws Exception
    {
        theConnectPlugin = installConnectPlugin();

        assertEquals("first enabled check failed", PluginState.ENABLED, theConnectPlugin.getPluginState());

        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "second check");
    }

    @Test
    public void disablingAddonSucceeds() throws Exception
    {
        theConnectPlugin = installConnectPlugin();

        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");

        pluginController.disablePlugin(singleModuleAddon.getKey());

        assertStateAndModuleCount(singleModuleAddon, PluginState.DISABLED, 0, "second check");

    }

    @Test
    public void disablingThenEnablingAddonSucceeds() throws Exception
    {
        theConnectPlugin = installConnectPlugin();

        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");

        pluginController.disablePlugin(singleModuleAddon.getKey());

        assertStateAndModuleCount(singleModuleAddon, PluginState.DISABLED, 0, "second check");

        pluginController.enablePlugins(singleModuleAddon.getKey());

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "third check");

    }

    @Test
    public void disablingConnectDisablesAddon() throws Exception
    {
        theConnectPlugin = installConnectPlugin();
        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);

        assertEquals("enabled check failed", PluginState.ENABLED, singleModuleAddon.getPluginState());

        pluginController.disablePlugin(theConnectPlugin.getKey());

        assertEquals(PluginState.DISABLED,theConnectPlugin.getPluginState());
        assertStateAndModuleCount(singleModuleAddon, PluginState.DISABLED, 0);

    }

    @Test
    public void disablingThenEnablingConnectDisablesAndEnablesAddon() throws Exception
    {
        theConnectPlugin = installConnectPlugin();
        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");

        pluginController.disablePlugin(theConnectPlugin.getKey());

        assertStateAndModuleCount(singleModuleAddon, PluginState.DISABLED, 0, "second check");

        pluginController.enablePlugins(theConnectPlugin.getKey());

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "third check");
    }

    @Test
    public void uninstallingConnectDisablesAddon() throws Exception
    {
        theConnectPlugin = installConnectPlugin();
        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");

        pluginController.uninstall(theConnectPlugin);
        theConnectPlugin = null;

        assertStateAndModuleCount(singleModuleAddon, PluginState.DISABLED, 0, "second check");
    }

    @Test
    public void uninstallingAndThenInstallingConnectDisablesAndEnablesAddon() throws Exception
    {
        theConnectPlugin = installConnectPlugin();
        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");

        pluginController.uninstall(theConnectPlugin);
        theConnectPlugin = null;

        assertStateAndModuleCount(singleModuleAddon, PluginState.DISABLED, 0, "second check");

        theConnectPlugin = installConnectPlugin();

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "third check");

    }

    @Test
    public void upgradingConnectEnablesAddon() throws Exception
    {
        theConnectPlugin = installConnectPlugin();
        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");

        theConnectPlugin = installConnectPlugin();

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "second check");

    }

    @Test
    public void disablingConnectDisablesMultipleAddons() throws Exception
    {
        theConnectPlugin = installConnectPlugin();
        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);
        doubleModuleAddon = installAddon(DOUBLE_MODULE_ADDON);

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "first check");

        pluginController.disablePlugin(theConnectPlugin.getKey());

        assertStateAndModuleCount(singleModuleAddon, PluginState.DISABLED, 0, "second check");
        assertStateAndModuleCount(singleModuleAddon, PluginState.DISABLED, 0, "second check");

    }

    @Test
    public void disablingThenEnablingConnectDisablesAndEnablesMultipleAddons() throws Exception
    {
        theConnectPlugin = installConnectPlugin();
        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);
        doubleModuleAddon = installAddon(DOUBLE_MODULE_ADDON);

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "first check");

        pluginController.disablePlugin(theConnectPlugin.getKey());

        assertStateAndModuleCount(singleModuleAddon, PluginState.DISABLED, 0, "second check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.DISABLED, 0, "second check");

        pluginController.enablePlugins(theConnectPlugin.getKey());

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "third check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "third check");

    }

    @Test
    public void uninstallingConnectDisablesMultipleAddons() throws Exception
    {
        theConnectPlugin = installConnectPlugin();
        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);
        doubleModuleAddon = installAddon(DOUBLE_MODULE_ADDON);

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "first check");

        pluginController.uninstall(theConnectPlugin);
        theConnectPlugin = null;

        assertStateAndModuleCount(singleModuleAddon, PluginState.DISABLED, 0, "second check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.DISABLED, 0, "second check");

    }

    @Test
    public void uninstallingAndThenInstallingConnectDisablesAndEnablesMultipleAddons() throws Exception
    {
        theConnectPlugin = installConnectPlugin();
        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);
        doubleModuleAddon = installAddon(DOUBLE_MODULE_ADDON);

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "first check");

        pluginController.uninstall(theConnectPlugin);
        theConnectPlugin = null;

        assertStateAndModuleCount(singleModuleAddon, PluginState.DISABLED, 0, "second check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.DISABLED, 0, "second check");

        theConnectPlugin = installConnectPlugin();

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "third check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "third check");

    }

    @Test
    public void upgradingConnectEnablesMultipleAddons() throws Exception
    {
        theConnectPlugin = installConnectPlugin();
        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);
        doubleModuleAddon = installAddon(DOUBLE_MODULE_ADDON);

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "first check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "first check");

        theConnectPlugin = installConnectPlugin();

        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "second check");
        assertStateAndModuleCount(doubleModuleAddon, PluginState.ENABLED, 2, "second check");

    }

    @Test
    public void upgradingFromPre1_0ConnectEnablesAddon() throws Exception
    {
        theOLDConnectPlugin = installOLDConnectPlugin();
        singleModuleAddon = installAddon(SINGLE_MODULE_ADDON);

        assertEquals(PluginState.ENABLED, theOLDConnectPlugin.getPluginState());
        assertEquals(PluginState.ENABLED, singleModuleAddon.getPluginState());

        theConnectPlugin = installConnectPlugin();

        assertEquals(PluginState.ENABLED, theConnectPlugin.getPluginState());
        assertStateAndModuleCount(singleModuleAddon, PluginState.ENABLED, 1, "second check");

    }

    private Plugin installConnectPlugin() throws IOException
    {
        return testPluginInstaller.installPlugin(getConnectPluginJar());
    }

    private Plugin installOLDConnectPlugin() throws IOException
    {
        return testPluginInstaller.installPlugin(getOLDConnectPluginJar());
    }

    private Plugin installAddon(String template) throws IOException
    {
        return testPluginInstaller.installAddon(getAddonJson(template));
    }

    private File getConnectPluginJar() throws IOException
    {
        InputStream is = this.getClass().getResourceAsStream(CONNECT_PLUGIN_PATH);
        String path = IOUtils.toString(is);

        return new File(path);
    }

    private File getOLDConnectPluginJar() throws IOException
    {
        InputStream is = this.getClass().getResourceAsStream(OLD_CONNECT_PLUGIN_PATH);
        String path = IOUtils.toString(is);

        return new File(path);
    }

    private String getAddonJson(String template) throws IOException
    {
        InputStream is = this.getClass().getResourceAsStream(JSON_TEMPLATE_PREFIX + template);
        String json = IOUtils.toString(is);

        return json;
    }
    
    private void clearConnectPluginSettings()
    {
        PluginSettings settings = settings();
        
        for(String key : settingKeys)
        {
            settings.remove(key);
        }
    }

    private PluginSettings settings()
    {
        return pluginSettingsFactory.createGlobalSettings();
    }

    private void assertStateAndModuleCount(Plugin singleModuleAddon, PluginState state, int moduleCount)
    {
        assertStateAndModuleCount(singleModuleAddon, state, moduleCount, "");
    }

    private void assertStateAndModuleCount(Plugin singleModuleAddon, PluginState state, int moduleCount, String prefix)
    {
        StringBuilder sb = new StringBuilder();

        if (!Strings.isNullOrEmpty(prefix))
        {
            sb.append(prefix).append(" - ");
        }

        sb.append(singleModuleAddon.getKey()).append(": ");

        PluginState addonState = singleModuleAddon.getPluginState();
        Collection<ModuleDescriptor<?>> addonModules = singleModuleAddon.getModuleDescriptors();
        int addonModuleCount = addonModules.size();

        boolean failed = false;
        if (!state.equals(addonState))
        {
            sb.append("expected state ").append(state.name()).append(" but was ").append(addonState.name());
            failed = true;
        }
        if (moduleCount != addonModuleCount)
        {
            sb.append(", expected module size ").append(moduleCount).append(" but was ").append(addonModuleCount);
            failed = true;
        }

        if (failed)
        {
            fail(sb.toString());
        }
    }
}
