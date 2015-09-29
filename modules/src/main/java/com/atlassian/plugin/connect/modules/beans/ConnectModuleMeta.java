package com.atlassian.plugin.connect.modules.beans;

public interface ConnectModuleMeta<T extends ModuleBean>
{
    boolean multipleModulesAllowed();

    String getDescriptorKey();

    Class<T> getBeanClass();
}
