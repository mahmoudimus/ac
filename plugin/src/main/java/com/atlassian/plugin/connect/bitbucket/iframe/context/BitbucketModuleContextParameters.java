package com.atlassian.plugin.connect.bitbucket.iframe.context;

import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.repository.Repository;

/**
 * @since 1.1.29
 */
public interface BitbucketModuleContextParameters extends ModuleContextParameters
{
    void addProject(Project project);

    void addRepository(Repository repository);
}
