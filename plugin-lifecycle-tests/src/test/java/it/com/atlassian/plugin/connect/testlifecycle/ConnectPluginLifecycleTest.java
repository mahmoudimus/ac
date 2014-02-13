package it.com.atlassian.plugin.connect.testlifecycle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import com.google.common.base.Strings;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AtlassianPluginsTestRunner.class)
public class ConnectPluginLifecycleTest
{
    public static final String CONNECT_PLUGIN_PATH = "/connect-plugin-path.txt";
    public static final String JSON_TEMPLATE_PREFIX = "/json/";
    public static final String SINGLE_MODULE_ADDON = "single-module.json";
    public static final String DOUBLE_MODULE_ADDON = "double-module.json";
    
    private final LifecyclePluginInstaller testPluginInstaller;
    private final PluginController pluginController;
    
    public ConnectPluginLifecycleTest(LifecyclePluginInstaller testPluginInstaller, PluginController pluginController)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.pluginController = pluginController;
    }

    @Test
    public void installConnectSucceeds() throws Exception
    {
        Plugin connectPlugin = null;
        try
        {
            connectPlugin = installConnectPlugin();
            
            assertEquals(PluginState.ENABLED,connectPlugin.getPluginState());
        }
        finally
        {
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    @Test
    public void installConnectWithAddonSucceeds() throws Exception
    {
        Plugin connectPlugin = null;
        Plugin addonPlugin = null;
        try
        {
            connectPlugin = installConnectPlugin();

            assertEquals("first enabled check failed", PluginState.ENABLED,connectPlugin.getPluginState());

            addonPlugin = installAddon(SINGLE_MODULE_ADDON);
            
            assertStateAndModuleCount(addonPlugin,PluginState.ENABLED,1,"second check");
        }
        finally
        {
            if(null != addonPlugin)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin);
            }
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    @Test
    public void disablingAddonSucceeds() throws Exception
    {
        Plugin connectPlugin = null;
        Plugin addonPlugin = null;
        try
        {
            connectPlugin = installConnectPlugin();

            addonPlugin = installAddon(SINGLE_MODULE_ADDON);

            assertStateAndModuleCount(addonPlugin,PluginState.ENABLED,1,"first check");
            
            pluginController.disablePlugin(addonPlugin.getKey());

            assertStateAndModuleCount(addonPlugin,PluginState.DISABLED,0,"second check");
            
        }
        finally
        {
            if(null != addonPlugin)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin);
            }
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    @Test
    public void disablingThenEnablingAddonSucceeds() throws Exception
    {
        Plugin connectPlugin = null;
        Plugin addonPlugin = null;
        try
        {
            connectPlugin = installConnectPlugin();

            addonPlugin = installAddon(SINGLE_MODULE_ADDON);

            assertStateAndModuleCount(addonPlugin,PluginState.ENABLED,1,"first check");

            pluginController.disablePlugin(addonPlugin.getKey());

            assertStateAndModuleCount(addonPlugin,PluginState.DISABLED,0,"second check");
            
            pluginController.enablePlugins(addonPlugin.getKey());

            assertStateAndModuleCount(addonPlugin,PluginState.ENABLED,1,"third check");

        }
        finally
        {
            if(null != addonPlugin)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin);
            }
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    @Test
    public void disablingConnectDisablesAddon() throws Exception
    {
        Plugin connectPlugin = null;
        Plugin addonPlugin = null;
        try
        {
            connectPlugin = installConnectPlugin();
            addonPlugin = installAddon(SINGLE_MODULE_ADDON);

            assertEquals("enabled check failed", PluginState.ENABLED,addonPlugin.getPluginState());
            
            pluginController.disablePlugin(connectPlugin.getKey());

            assertStateAndModuleCount(addonPlugin,PluginState.DISABLED,0);
            
        }
        finally
        {
            if(null != addonPlugin)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin);
            }
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    @Test
    public void disablingThenEnablingConnectDisablesAndEnablesAddon() throws Exception
    {
        Plugin connectPlugin = null;
        Plugin addonPlugin = null;
        try
        {
            connectPlugin = installConnectPlugin();
            addonPlugin = installAddon(SINGLE_MODULE_ADDON);

            assertStateAndModuleCount(addonPlugin,PluginState.ENABLED,1,"first check");

            pluginController.disablePlugin(connectPlugin.getKey());

            assertStateAndModuleCount(addonPlugin,PluginState.DISABLED,0,"second check");
            
            pluginController.enablePlugins(connectPlugin.getKey());

            assertStateAndModuleCount(addonPlugin,PluginState.ENABLED,1, "third check");

        }
        finally
        {
            if(null != addonPlugin)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin);
            }
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    @Test
    public void uninstallingConnectDisablesAddon() throws Exception
    {
        Plugin connectPlugin = null;
        Plugin addonPlugin = null;
        try
        {
            connectPlugin = installConnectPlugin();
            addonPlugin = installAddon(SINGLE_MODULE_ADDON);

            assertStateAndModuleCount(addonPlugin, PluginState.ENABLED, 1, "first check");

            pluginController.uninstall(connectPlugin);
            connectPlugin = null;

            assertStateAndModuleCount(addonPlugin,PluginState.DISABLED,0,"second check");

        }
        finally
        {
            if(null != addonPlugin)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin);
            }
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    @Test
    public void uninstallingAndThenInstallingConnectDisablesAndEnablesAddon() throws Exception
    {
        Plugin connectPlugin = null;
        Plugin addonPlugin = null;
        try
        {
            connectPlugin = installConnectPlugin();
            addonPlugin = installAddon(SINGLE_MODULE_ADDON);

            assertStateAndModuleCount(addonPlugin, PluginState.ENABLED, 1, "first check");

            pluginController.uninstall(connectPlugin);
            connectPlugin = null;

            assertStateAndModuleCount(addonPlugin, PluginState.DISABLED, 0, "second check");
            
            connectPlugin = installConnectPlugin();

            assertStateAndModuleCount(addonPlugin, PluginState.ENABLED, 1, "third check");

        }
        finally
        {
            if(null != addonPlugin)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin);
            }
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    @Test
    public void upgradingConnectEnablesAddon() throws Exception
    {
        Plugin connectPlugin = null;
        Plugin addonPlugin = null;
        try
        {
            connectPlugin = installConnectPlugin();
            addonPlugin = installAddon(SINGLE_MODULE_ADDON);
            
            assertStateAndModuleCount(addonPlugin,PluginState.ENABLED,1, "first check");

            connectPlugin = installConnectPlugin();

            assertStateAndModuleCount(addonPlugin,PluginState.ENABLED,1, "second check");

        }
        finally
        {
            if(null != addonPlugin)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin);
            }
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    @Test
    public void disablingConnectDisablesMultipleAddons() throws Exception
    {
        Plugin connectPlugin = null;
        Plugin addonPlugin = null;
        Plugin addonPlugin2 = null;
        try
        {
            connectPlugin = installConnectPlugin();
            addonPlugin = installAddon(SINGLE_MODULE_ADDON);
            addonPlugin2 = installAddon(DOUBLE_MODULE_ADDON);

            assertStateAndModuleCount(addonPlugin,PluginState.ENABLED,1, "first check");
            assertStateAndModuleCount(addonPlugin2,PluginState.ENABLED,2, "first check");

            pluginController.disablePlugin(connectPlugin.getKey());

            assertStateAndModuleCount(addonPlugin,PluginState.DISABLED,0, "second check");
            assertStateAndModuleCount(addonPlugin,PluginState.DISABLED,0, "second check");

        }
        finally
        {
            if(null != addonPlugin)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin);
            }
            if(null != addonPlugin2)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin2);
            }
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    @Test
    public void disablingThenEnablingConnectDisablesAndEnablesMultipleAddons() throws Exception
    {
        Plugin connectPlugin = null;
        Plugin addonPlugin = null;
        Plugin addonPlugin2 = null;
        try
        {
            connectPlugin = installConnectPlugin();
            addonPlugin = installAddon(SINGLE_MODULE_ADDON);
            addonPlugin2 = installAddon(DOUBLE_MODULE_ADDON);

            assertStateAndModuleCount(addonPlugin, PluginState.ENABLED, 1, "first check");
            assertStateAndModuleCount(addonPlugin2, PluginState.ENABLED, 2, "first check");

            pluginController.disablePlugin(connectPlugin.getKey());

            assertStateAndModuleCount(addonPlugin, PluginState.DISABLED, 0, "second check");
            assertStateAndModuleCount(addonPlugin2, PluginState.DISABLED, 0, "second check");

            pluginController.enablePlugins(connectPlugin.getKey());

            assertStateAndModuleCount(addonPlugin, PluginState.ENABLED, 1, "third check");
            assertStateAndModuleCount(addonPlugin2, PluginState.ENABLED, 2, "third check");

        }
        finally
        {
            if(null != addonPlugin)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin);
            }
            if(null != addonPlugin2)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin2);
            }
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    @Test
    public void uninstallingConnectDisablesMultipleAddons() throws Exception
    {
        Plugin connectPlugin = null;
        Plugin addonPlugin = null;
        Plugin addonPlugin2 = null;
        try
        {
            connectPlugin = installConnectPlugin();
            addonPlugin = installAddon(SINGLE_MODULE_ADDON);
            addonPlugin2 = installAddon(DOUBLE_MODULE_ADDON);

            assertStateAndModuleCount(addonPlugin,PluginState.ENABLED,1, "first check");
            assertStateAndModuleCount(addonPlugin2,PluginState.ENABLED,2, "first check");

            pluginController.uninstall(connectPlugin);
            connectPlugin = null;

            assertStateAndModuleCount(addonPlugin, PluginState.DISABLED, 0, "second check");
            assertStateAndModuleCount(addonPlugin2, PluginState.DISABLED, 0, "second check");

        }
        finally
        {
            if(null != addonPlugin)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin);
            }
            if(null != addonPlugin2)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin2);
            }
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    @Test
    public void uninstallingAndThenInstallingConnectDisablesAndEnablesMultipleAddons() throws Exception
    {
        Plugin connectPlugin = null;
        Plugin addonPlugin = null;
        Plugin addonPlugin2 = null;
        try
        {
            connectPlugin = installConnectPlugin();
            addonPlugin = installAddon(SINGLE_MODULE_ADDON);
            addonPlugin2 = installAddon(DOUBLE_MODULE_ADDON);

            assertStateAndModuleCount(addonPlugin, PluginState.ENABLED, 1, "first check");
            assertStateAndModuleCount(addonPlugin2, PluginState.ENABLED, 2, "first check");

            pluginController.uninstall(connectPlugin);
            connectPlugin = null;

            assertStateAndModuleCount(addonPlugin, PluginState.DISABLED, 0, "second check");
            assertStateAndModuleCount(addonPlugin2, PluginState.DISABLED, 0, "second check");

            connectPlugin = installConnectPlugin();

            assertStateAndModuleCount(addonPlugin, PluginState.ENABLED, 1, "third check");
            assertStateAndModuleCount(addonPlugin2, PluginState.ENABLED, 2, "third check");

        }
        finally
        {
            if(null != addonPlugin)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin);
            }
            if(null != addonPlugin2)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin2);
            }
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    @Test
    public void upgradingConnectEnablesMultipleAddons() throws Exception
    {
        Plugin connectPlugin = null;
        Plugin addonPlugin = null;
        Plugin addonPlugin2 = null;
        try
        {
            connectPlugin = installConnectPlugin();
            addonPlugin = installAddon(SINGLE_MODULE_ADDON);
            addonPlugin2 = installAddon(DOUBLE_MODULE_ADDON);

            assertStateAndModuleCount(addonPlugin, PluginState.ENABLED, 1, "first check");
            assertStateAndModuleCount(addonPlugin2, PluginState.ENABLED, 2, "first check");

            connectPlugin = installConnectPlugin();

            assertStateAndModuleCount(addonPlugin, PluginState.ENABLED, 1, "second check");
            assertStateAndModuleCount(addonPlugin2, PluginState.ENABLED, 2, "second check");

        }
        finally
        {
            if(null != addonPlugin)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin);
            }
            if(null != addonPlugin2)
            {
                testPluginInstaller.uninstallPlugin(addonPlugin2);
            }
            if(null != connectPlugin)
            {
                testPluginInstaller.uninstallPlugin(connectPlugin);
            }
        }
    }

    private Plugin installConnectPlugin() throws IOException
    {
        return testPluginInstaller.installPlugin(getConnectPluginJar());
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

    private String getAddonJson(String template) throws IOException
    {
        InputStream is = this.getClass().getResourceAsStream(JSON_TEMPLATE_PREFIX + template);
        String json = IOUtils.toString(is);

        return json;
    }

    private void assertStateAndModuleCount(Plugin addonPlugin, PluginState state, int moduleCount)
    {
        assertStateAndModuleCount(addonPlugin, state, moduleCount,"");
    }

    private void assertStateAndModuleCount(Plugin addonPlugin, PluginState state, int moduleCount, String prefix)
    {
        StringBuilder sb = new StringBuilder();
        
        if(!Strings.isNullOrEmpty(prefix))
        {
            sb.append(prefix).append(" - ");
        }
        
        sb.append(addonPlugin.getKey()).append(": ");
        
        PluginState addonState = addonPlugin.getPluginState();
        Collection<ModuleDescriptor<?>> addonModules = addonPlugin.getModuleDescriptors();
        int addonModuleCount = addonModules.size();

        boolean failed = false;
        if(!state.equals(addonState))
        {
            sb.append("expected state ").append(state.name()).append(" but was ").append(addonState.name());
            failed = true;
        }
        if(moduleCount != addonModuleCount)
        {
            sb.append(", expected module size ").append(moduleCount).append(" but was ").append(addonModuleCount);
            failed = true;
        }

        if(failed)
        {
            fail(sb.toString());
        }
    }
}
