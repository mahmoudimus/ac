package com.atlassian.plugin.connect.bitbucket.iframe.context;

import com.atlassian.plugin.connect.spi.iframe.context.HashMapModuleContextParameters;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.repository.Repository;

public class BitbucketModuleContextParametersImpl
        extends HashMapModuleContextParameters implements BitbucketModuleContextParameters
{
    @Override
    public void addProject(final Project project)
    {
        if (project != null)
        {
            put(BitbucketModuleContextFilter.PROJECT_ID, String.valueOf(project.getId()));
        }
    }

    @Override
    public void addRepository(final Repository repository)
    {
        if (repository != null)
        {
            put(BitbucketModuleContextFilter.REPOSITORY_ID, String.valueOf(repository.getId()));
        }
    }
}
