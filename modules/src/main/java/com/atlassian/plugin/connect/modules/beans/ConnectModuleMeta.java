package com.atlassian.plugin.connect.modules.beans;

public interface ConnectModuleMeta
{
    boolean multipleModulesAllowed();

    String getDescriptorKey();

    Class getBeanClass();
}
