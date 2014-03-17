package com.atlassian.plugin.connect.plugin.installer;

import java.util.Collection;

import javax.inject.Named;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.upm.spi.PluginControlHandler;

@ExportAsService(PluginControlHandler.class)
@Named
public class ConnectUPMControlHandler implements PluginControlHandler
{
    private final ConnectAddonRegistry descriptorRegistry;

    public ConnectUPMControlHandler(ConnectAddonRegistry descriptorRegistry)
    {
        this.descriptorRegistry = descriptorRegistry;
    }

    @Override
    public boolean canControl(String pluginKey)
    {
        return descriptorRegistry.hasDescriptor(pluginKey);
    }

    @Override
    public void enablePlugins(Plugin... plugins)
    {

    }

    @Override
    public void enablePlugins(String... pluginKeys)
    {

    }

    @Override
    public boolean isPluginEnabled(String pluginKey)
    {
        return false;
    }

    @Override
    public void disablePlugin(String pluginKey)
    {

    }

    @Override
    public Plugin getPlugin(String pluginKey)
    {
        return null;
    }

    @Override
    public Collection<? extends Plugin> getPlugins()
    {
        return null;
    }

    @Override
    public void uninstall(Plugin plugin) throws PluginException
    {

    }
}
