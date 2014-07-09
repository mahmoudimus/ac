package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

public interface ConnectModuleProviderContext
{
    ConnectAddonBean getConnectAddonBean();

    ModuleLocationQualifier getLocationQualifier();
}

