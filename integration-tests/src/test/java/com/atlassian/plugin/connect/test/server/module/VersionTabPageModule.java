package com.atlassian.plugin.connect.test.server.module;

public class VersionTabPageModule extends MainModuleWithResource<VersionTabPageModule>
{
    public static final String VERSION_TAB_PAGE = "version-tab-page";

    protected VersionTabPageModule(final String key)
    {
        super(VERSION_TAB_PAGE, key);
    }

    public static VersionTabPageModule key(final String key)
    {
        return new VersionTabPageModule(key);
    }
}
