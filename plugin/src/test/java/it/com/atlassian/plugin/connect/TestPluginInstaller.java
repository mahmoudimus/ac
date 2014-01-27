package it.com.atlassian.plugin.connect;

import java.io.File;
import java.io.IOException;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallHandler;
import com.atlassian.upm.spi.PluginInstallResult;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class TestPluginInstaller
{
    public static final String DESCRIPTOR_PREFIX = "connect-descriptor-";
    private final PluginInstallHandler installHandler;
    private final PluginController pluginController;

    public TestPluginInstaller(PluginInstallHandler installHandler, PluginController pluginController)
    {
        this.installHandler = installHandler;
        this.pluginController = pluginController;
    }

    public Plugin installPlugin(ConnectAddonBean bean) throws IOException
    {
        String json = ConnectModulesGsonFactory.addonBeanToJson(bean);
        File descriptor = createTempDescriptor(json);

        PluginInstallResult result = installHandler.installPlugin(descriptor, Option.<String>some("application/json"));
        return result.getPlugin();
    }

    public void uninstallPlugin(Plugin plugin) throws IOException
    {
        pluginController.uninstall(plugin);
    }

    private File createTempDescriptor(String json) throws IOException
    {
        File tmpFile = File.createTempFile(ModuleKeyGenerator.randomName(DESCRIPTOR_PREFIX), ".json");
        Files.write(json,tmpFile, Charsets.UTF_8);
        
        return tmpFile;
    }
}
