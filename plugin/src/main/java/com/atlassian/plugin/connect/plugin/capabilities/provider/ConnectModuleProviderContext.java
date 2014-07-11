package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

/**
 * A context for ConnectModuleProviders.
 *
 * Provides data and helpers that are shared across modules for a single
 * addon bean
 */
public interface ConnectModuleProviderContext
{
    /**
     * The connect addon that owns the module being processed
     */
    ConnectAddonBean getConnectAddonBean();

    /**
     * A helper for qualifying location names so they match the qualified names of module keys
     */
    ModuleLocationQualifier getLocationQualifier();
}

