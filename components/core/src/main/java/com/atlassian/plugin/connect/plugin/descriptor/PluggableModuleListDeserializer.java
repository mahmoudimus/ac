package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.spi.descriptor.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.plugin.lifecycle.ConnectModuleProviderModuleDescriptor;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleProvider;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.google.gson.JsonElement;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluggableModuleListDeserializer extends ModuleListDeserializer
{

    protected final PluginAccessor pluginAccessor;

    public PluggableModuleListDeserializer(PluginAccessor pluginAccessor, ShallowConnectAddonBean addonBean)
    {
        super(addonBean);
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public List<ModuleBean> deserializeModules(final String moduleTypeKey, JsonElement modules) throws ConnectModuleValidationException
    {
        final ConnectModuleProvider moduleProvider = getModuleProviders().get(moduleTypeKey);
        if (moduleProvider == null)
        {
            throwUnknownModuleType(moduleTypeKey);
        }
        return moduleProvider.deserializeAddonDescriptorModules(modules.toString(), addon);
    }

    @Override
    public boolean multipleModulesAllowed(String moduleType)
    {
        return getModuleProviders().get(moduleType).getMeta().multipleModulesAllowed();
    }

    private Map<String, ConnectModuleProvider> getModuleProviders()
    {
        Collection<ConnectModuleProvider> moduleProviders = pluginAccessor.getModules(
                new ModuleDescriptorOfClassPredicate<>(ConnectModuleProviderModuleDescriptor.class));
        Map<String, ConnectModuleProvider> moduleProviderMap = new HashMap<>();
        for (ConnectModuleProvider moduleProvider : moduleProviders)
        {
            moduleProviderMap.put(moduleProvider.getMeta().getDescriptorKey(), moduleProvider);
        }
        return moduleProviderMap;
    }
}
