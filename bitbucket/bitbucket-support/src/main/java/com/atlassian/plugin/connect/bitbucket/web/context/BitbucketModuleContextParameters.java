package com.atlassian.plugin.connect.bitbucket.web.context;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;

/**
 * @since 1.1.29
 */
public interface BitbucketModuleContextParameters extends ModuleContextParameters
{
    void addProject(Project project);

    void addRepository(Repository repository);
}
