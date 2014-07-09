package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

/**
 * A context for ConnectModuleProvider's
 */
public interface ConnectModuleProviderContext
{
    ConnectAddonBean getConnectAddonBean();

    /**
     * A helper for qualifying location names so they match the qualified names of module keys
     */
    ModuleLocationQualifier getLocationQualifier();
}

