package com.atlassian.plugin.connect.test.server.module.jira;

import com.atlassian.plugin.connect.test.server.module.MainModuleWithResource;

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
