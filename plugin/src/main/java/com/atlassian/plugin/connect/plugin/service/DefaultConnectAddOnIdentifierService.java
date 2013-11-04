package com.atlassian.plugin.connect.plugin.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.osgi.framework.Bundle;

/**
 * @since 1.0
 */
public class DefaultConnectAddOnIdentifierService implements ConnectAddOnIdentifierService
{
    private final PluginAccessor pluginAccessor;

    public DefaultConnectAddOnIdentifierService(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public boolean isConnectAddOn(final Bundle bundle)
    {
        try
        {
            return (bundle.getHeaders() != null && (bundle.getHeaders().get(CONNECT_HEADER) != null));
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
           
            return mf.getMainAttributes().containsKey(new Attributes.Name(CONNECT_HEADER));
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
        try
        {
            Element root = pluginDescriptor.getRootElement();
            return (null != root.element("remote-plugin-container"));
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public boolean isConnectAddOn(final File descriptorFile)
    {
        try
        {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(descriptorFile);
            
            return isConnectAddOn(doc);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public String getInstallerUser(Bundle bundle)
    {
        String header = (String) bundle.getHeaders().get(CONNECT_HEADER);
        if (header != null)
        {
            return OsgiHeaderUtil.parseHeader(header).get("installer").get("user");
        }
        return null;
    }

    @Override
    public String getRegistrationUrl(Bundle bundle)
    {
        String header = (String) bundle.getHeaders().get(CONNECT_HEADER);
        if (header != null)
        {
            return OsgiHeaderUtil.parseHeader(header).get("installer").get("registration-url");
        }
        return null;
    }
}
