package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public interface ConnectModuleProviderRegistry
{
    ConnectModuleProvider<?> get(String moduleKey);

    void register(String moduleKey, ConnectModuleProvider<?> provider);
}

class ConnectModuleProviderRegistryImpl implements ConnectModuleProviderRegistry
{
    private Map<String, ConnectModuleProvider<?>> providerMap = newHashMap();

    @Override
    public ConnectModuleProvider<?> get(String moduleKey)
    {
        return providerMap.get(moduleKey);
    }

    @Override
    public void register(String moduleKey, ConnectModuleProvider<?> provider)
    {
        providerMap.put(moduleKey, provider);
    }
}