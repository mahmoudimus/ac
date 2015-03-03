package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.devstatus.releasereport.warnings.VersionWarningCategoryModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

public class ConnectVersionWarningCategoryModuleDescriptor extends VersionWarningCategoryModuleDescriptor
{
    public ConnectVersionWarningCategoryModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }
}
