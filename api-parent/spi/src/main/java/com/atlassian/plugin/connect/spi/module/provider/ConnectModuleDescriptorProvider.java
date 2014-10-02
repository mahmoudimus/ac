package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.ModuleDescriptor;

import java.util.List;

public interface ConnectModuleDescriptorProvider<T>
{
    List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, List<T> modules);
}
