package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.plugin.lifecycle.ConnectModuleProviderModuleDescriptor;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.descriptor.ConnectModuleValidationException;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.google.gson.JsonElement;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginAvailableModuleTypes implements AvailableModuleTypes
{
    private final Map<String, ConnectModuleProvider> moduleProviders;
    private final ShallowConnectAddonBean addonBean;

    public PluginAvailableModuleTypes(PluginAccessor pluginAccessor, ShallowConnectAddonBean addonBean)
    {
        this.moduleProviders = buildModuleProviderMap(pluginAccessor.getModules(new ModuleDescriptorOfClassPredicate<>(ConnectModuleProviderModuleDescriptor.class)));
        this.addonBean = addonBean;
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
    public List<ModuleBean> deserializeModules(String moduleTypeKey, JsonElement modules) throws ConnectModuleValidationException
    {
        final ConnectModuleProvider moduleProvider = moduleProviders.get(moduleTypeKey);
        return moduleProvider.deserializeAddonDescriptorModules(modules.toString(), addonBean);
    }
    
    @Override
    public boolean multipleModulesAllowed(String moduleType)
    {
        return moduleProviders.get(moduleType).getMeta().multipleModulesAllowed();
    }
    
    @Override
    public boolean validModuleType(String moduleType)
    {
        return moduleProviders.keySet().contains(moduleType);
    }
}
