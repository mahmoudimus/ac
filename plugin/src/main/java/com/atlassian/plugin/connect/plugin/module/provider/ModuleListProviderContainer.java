package com.atlassian.plugin.connect.plugin.module.provider;


import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

public interface ModuleListProviderContainer
{
    Iterable<ModuleListProviderFactory> provideFactories(final ConnectAddonBean addon);
}
