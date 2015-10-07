package com.atlassian.plugin.connect.spi.module;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;

import java.util.List;

public interface ConnectModuleProvider<T extends BaseModuleBean>
{
    String getSchemaPrefix();

    ConnectModuleMeta<T> getMeta();

    List<T> validate(String modules, Class<T> type, Plugin plugin, ShallowConnectAddonBean bean) throws ConnectModuleValidationException;

    List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin plugin, List<T> beans);
}