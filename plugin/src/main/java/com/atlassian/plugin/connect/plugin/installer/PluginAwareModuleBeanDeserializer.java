package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.plugin.descriptor.ConnectModuleProviderModuleDescriptor;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleValidationException;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.google.gson.JsonElement;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginAwareModuleBeanDeserializer extends ModuleBeanDeserializer
{
    private final Map<String, ConnectModuleProvider> moduleProviders;
    private final Plugin plugin;

    public PluginAwareModuleBeanDeserializer(PluginAccessor pluginAccessor)
    {
        plugin = pluginAccessor.getEnabledPlugin("com.atlassian.plugins.atlassian-connect-plugin");
        this.moduleProviders = buildModuleProviderMap(pluginAccessor.getModules(new ModuleDescriptorOfClassPredicate<>(ConnectModuleProviderModuleDescriptor.class)));
    }

    private Map<String, ConnectModuleProvider> buildModuleProviderMap(Collection<ConnectModuleProvider> moduleProviders)
    {
        Map<String, ConnectModuleProvider> moduleProviderMap = new HashMap<>();
        for (ConnectModuleProvider moduleProvider : moduleProviders)
        {
            moduleProviderMap.put(moduleProvider.getMeta().getDescriptorKey(), moduleProvider);
        }
        return moduleProviderMap;
    }

    @Override
    protected List<ModuleBean> deserializeModulesOfSameType(Map.Entry<String, JsonElement> moduleEntry) throws ConnectModuleValidationException
    {
        final ConnectModuleProvider moduleProvider = moduleProviders.get(moduleEntry.getKey());
        return moduleProvider.validate(moduleEntry.getValue().toString(), moduleProvider.getMeta().getBeanClass(), plugin);
    }
    
    @Override
    protected boolean multipleModulesAllowed(String moduleType)
    {
        return moduleProviders.get(moduleType).getMeta().multipleModulesAllowed();
    }
    
    @Override
    protected boolean validModuleType(String moduleType)
    {
        return moduleProviders.keySet().contains(moduleType);
    }
}
