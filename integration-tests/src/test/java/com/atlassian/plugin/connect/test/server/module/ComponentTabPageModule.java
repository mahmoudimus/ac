package com.atlassian.plugin.connect.test.server.module;

public final class ComponentTabPageModule extends MainModuleWithResource<ComponentTabPageModule>
{

    public static final String COMPONENT_TAB_PAGE = "component-tab-page";

    private ComponentTabPageModule(String key)
    {
        super(COMPONENT_TAB_PAGE, key);
    }

    public static ComponentTabPageModule key(String key)
    {
        return new ComponentTabPageModule(key);
    }
}
