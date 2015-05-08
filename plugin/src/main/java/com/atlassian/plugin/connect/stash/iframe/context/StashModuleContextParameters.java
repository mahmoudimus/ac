package com.atlassian.plugin.connect.stash.iframe.context;

import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;
import com.atlassian.stash.project.Project;
import com.atlassian.stash.repository.Repository;

/**
 * @since 1.1.29
 */
public interface StashModuleContextParameters extends ModuleContextParameters
{
    void addProject(Project project);

    void addRepository(Repository repository);
}
