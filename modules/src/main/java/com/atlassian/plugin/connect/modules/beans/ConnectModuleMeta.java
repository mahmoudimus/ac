package com.atlassian.plugin.connect.modules.beans;

public interface ConnectModuleMeta<T extends ModuleBean>
{

    String getDescriptorKey();

    Class<T> getBeanClass();

    boolean multipleModulesAllowed();
}
