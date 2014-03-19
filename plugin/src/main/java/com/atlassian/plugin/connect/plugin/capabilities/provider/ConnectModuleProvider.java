package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

/**
 * @since 1.0
 */
public interface ConnectModuleProvider<T>
{
    List<ModuleDescriptor> provideModules(ConnectAddonBean addon, Plugin plugin, String jsonFieldName, List<T> beans);
}
