package com.atlassian.plugin.remotable.test.server.module;

public final class IssuePanelPageModule extends MainModuleWithResource<IssuePanelPageModule>
{
    private IssuePanelPageModule(String key)
    {
        super("issue-panel-page", key);
    }

    public static IssuePanelPageModule key(String key)
    {
        return new IssuePanelPageModule(key);
    }
}
