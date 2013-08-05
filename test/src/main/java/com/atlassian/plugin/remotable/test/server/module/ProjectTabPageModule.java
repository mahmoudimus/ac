package com.atlassian.plugin.remotable.test.server.module;

public final class ProjectTabPageModule extends MainModuleWithResource<ProjectTabPageModule>
{
    private ProjectTabPageModule(String key)
    {
        super("project-tab-page", key);
    }

    public static ProjectTabPageModule key(String key)
    {
        return new ProjectTabPageModule(key);
    }
}
