package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ModuleJson;

import java.util.List;

public interface TestConnectModuleProvider
{
    List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin plugin, List<ModuleJson> beans);
}