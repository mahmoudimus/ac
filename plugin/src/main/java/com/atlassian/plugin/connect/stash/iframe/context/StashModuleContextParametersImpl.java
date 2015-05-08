package com.atlassian.plugin.connect.stash.iframe.context;

import com.atlassian.plugin.connect.spi.iframe.context.HashMapModuleContextParameters;
import com.atlassian.stash.project.Project;
import com.atlassian.stash.repository.Repository;

public class StashModuleContextParametersImpl extends HashMapModuleContextParameters implements StashModuleContextParameters
{
    @Override
    public void addProject(final Project project)
    {
        if (project != null)
        {
            put(StashModuleContextFilter.PROJECT_ID, String.valueOf(project.getId()));
        }
    }

    @Override
    public void addRepository(final Repository repository)
    {
        if (repository != null)
        {
            put(StashModuleContextFilter.REPOSITORY_ID, String.valueOf(repository.getId()));
        }
    }
}
