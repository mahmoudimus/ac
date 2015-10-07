package com.atlassian.plugin.connect.spi.module;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.spi.module.ModuleLocationQualifier;

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
     *
     * @return the add-on bean
     */
    ConnectAddonBean getConnectAddonBean();

    /**
     * A helper for qualifying location names so they match the qualified names of module keys
     *
     * @return the location qualifier
     */
    ModuleLocationQualifier getLocationQualifier();
}

