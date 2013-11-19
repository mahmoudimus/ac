package com.atlassian.plugin.connect.plugin.capabilities;

import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.osgi.framework.Bundle;

@Named("jsonConnectAddOnIdentifierService")
public class JsonConnectAddOnIdentifierService implements ConnectAddOnIdentifierService
{

    private final PluginAccessor pluginAccessor;

    @Inject
    public JsonConnectAddOnIdentifierService(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
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
            return false;
        }
    }

    @Override
    public boolean isConnectAddOn(final Plugin plugin)
    {
        try
        {
            Manifest mf = new Manifest(plugin.getResourceAsStream("/META-INF/MANIFEST.MF"));

            return mf.getMainAttributes().containsKey(new Attributes.Name(CONNECT_ADDON_HEADER));
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public boolean isConnectAddOn(final String pluginKey)
    {
        return isConnectAddOn(pluginAccessor.getPlugin(pluginKey));
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
