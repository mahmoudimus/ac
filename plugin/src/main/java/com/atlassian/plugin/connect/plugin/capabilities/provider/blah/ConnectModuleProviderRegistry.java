package com.atlassian.plugin.connect.plugin.capabilities.provider.blah;

import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public interface ConnectModuleProviderRegistry
{
    ConnectModuleProvider<?> get(String moduleKey);

    void register(String moduleKey, ConnectModuleProvider<?> provider);
}

