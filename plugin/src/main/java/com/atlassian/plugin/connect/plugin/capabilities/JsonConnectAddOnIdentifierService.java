package com.atlassian.plugin.connect.plugin.capabilities;

import java.io.File;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;

import org.dom4j.Document;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("jsonConnectAddOnIdentifierService")
public class JsonConnectAddOnIdentifierService implements ConnectAddOnIdentifierService
{
    private static final Logger log = LoggerFactory.getLogger(JsonConnectAddOnIdentifierService.class);

    private final PluginAccessor pluginAccessor;
    private final ConnectAddonRegistry connectAddonRegistry;

    @Inject
    public JsonConnectAddOnIdentifierService(PluginAccessor pluginAccessor, ConnectAddonRegistry connectAddonRegistry)
    {
        this.pluginAccessor = pluginAccessor;
        this.connectAddonRegistry = connectAddonRegistry;
    }

    @Override
    public boolean isConnectAddOn(final Bundle bundle)
    {
        try
        {
            return (bundle.getHeaders() != null && (bundle.getHeaders().get(CONNECT_ADDON_HEADER) != null));
        }
        catch (Exception e)
        {
            log.debug("Exception reading bundle header " + CONNECT_ADDON_HEADER , e);
            return false;
        }
    }

    @Override
    public boolean isConnectAddOn(final Plugin plugin)
    {
        if (null != plugin)
        {
            try
            {
                InputStream resourceAsStream = plugin.getResourceAsStream("/META-INF/MANIFEST.MF");

                if (null != resourceAsStream)
                {
                    Manifest mf = new Manifest(resourceAsStream);
                    return mf.getMainAttributes().containsKey(new Attributes.Name(CONNECT_ADDON_HEADER));
                }
                else
                {
                    log.debug("Plugin '{}' has no MANIFEST.MF file. Defaulting to isConnectAddon=false.", plugin.getKey());
                }
            }
            catch (Exception e)
            {
                log.debug("Exception reading from MANIFEST.MF for plugin '{}'. Defaulting to isConnectAddon=false.", plugin.getKey(), e);
            }
        }

        return false;
    }

    @Override
    public boolean isConnectAddOn(final String pluginKey)
    {
        Plugin plugin = pluginAccessor.getPlugin(pluginKey);
        if(null != plugin)
        {
            return isConnectAddOn(plugin);
        }
        else
        {
            return connectAddonRegistry.hasDescriptor(pluginKey);
        }
    }

    @Override
    public boolean isConnectAddOn(final Document pluginDescriptor)
    {
        return false;
    }

    @Override
    public boolean isConnectAddOn(final File descriptorFile)
    {
        return false;
    }

    @Override
    public String getInstallerUser(Bundle bundle)
    {
        return null;
    }

    @Override
    public String getRegistrationUrl(Bundle bundle)
    {
        return null;
    }
}
