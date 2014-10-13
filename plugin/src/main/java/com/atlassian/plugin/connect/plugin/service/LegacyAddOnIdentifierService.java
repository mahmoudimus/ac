package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploder;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import org.osgi.framework.Bundle;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @since 1.0
 */
@Named("legacyAddOnIdentifierService")
@XmlDescriptor
public class LegacyAddOnIdentifierService implements ConnectAddOnIdentifierService
{
    private final PluginAccessor pluginAccessor;

    @Inject
    public LegacyAddOnIdentifierService(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public boolean isConnectAddOn(final Bundle bundle)
    {
        try
        {
            Object key = getPluginKeyFromBundle(bundle);
            XmlDescriptorExploder.notifyAndExplode(null == key ? null : key.toString());
            return null != key;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public boolean isConnectAddOn(final Plugin plugin)
    {
        XmlDescriptorExploder.notifyAndExplode(null == plugin ? null : plugin.getKey());

        try
        {
            Manifest mf = new Manifest(plugin.getResourceAsStream("/META-INF/MANIFEST.MF"));
           
            return mf.getMainAttributes().containsKey(new Attributes.Name(REMOTE_PLUGIN));
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

    private static Object getPluginKeyFromBundle(Bundle bundle)
    {
        return bundle.getHeaders() == null ? null : bundle.getHeaders().get(REMOTE_PLUGIN);
    }
}
