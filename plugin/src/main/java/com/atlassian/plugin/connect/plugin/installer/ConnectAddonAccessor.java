package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.predicate.PluginPredicate;
import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import static com.google.common.collect.Iterables.transform;

/**
 * A facade onto the PluginAccessor for Connect Addons
 */
@Named
public class ConnectAddonAccessor
{
    private static final Logger log = LoggerFactory.getLogger(ConnectAddonAccessor.class);

    private final JsonConnectAddOnIdentifierService connectIdentifier;
    private final ConnectAddonRegistry descriptorRegistry;
    private final PluginAccessor pluginAccessor;

    @Inject
    public ConnectAddonAccessor(JsonConnectAddOnIdentifierService connectIdentifier, ConnectAddonRegistry descriptorRegistry,
                                PluginAccessor pluginAccessor)
    {
        this.connectIdentifier = connectIdentifier;
        this.descriptorRegistry = descriptorRegistry;
        this.pluginAccessor = pluginAccessor;
    }


    public Iterable<ConnectAddonBean> fetchConnectAddons()
    {
        return transform(fetchConnectAddonDescriptors(), new Function<String, ConnectAddonBean>()
        {
            @Override
            public ConnectAddonBean apply(@Nullable String descriptorJsonStr)
            {
                return null == descriptorJsonStr ? null : ConnectModulesGsonFactory.getGson().fromJson(descriptorJsonStr, ConnectAddonBean.class);
            }
        });
    }

    public Iterable<String> fetchConnectAddonDescriptors()
    {
        return transform(pluginAccessor.getPlugins(new PluginPredicate()
        {
            @Override
            public boolean matches(Plugin plugin)
            {
                return null != plugin && connectIdentifier.isConnectAddOn(plugin);
            }
        }), new Function<Plugin, String>()
        {
            @Override
            public String apply(@Nullable Plugin plugin)
            {
                return null == plugin ? null : descriptorRegistry.getDescriptor(plugin.getKey());
            }
        });
    }
}
