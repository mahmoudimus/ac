package com.atlassian.plugin.connect.test.server.module;

public final class IssueTabPageModule extends MainModuleWithResource<IssueTabPageModule>
{
    private IssueTabPageModule(String key)
    {
        super("issue-tab-page", key);
    }

    public static IssueTabPageModule key(String key)
    {
        return new IssueTabPageModule(key);
    }
}
